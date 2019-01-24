/*
 * Plugin UHCReloaded : Alliances
 *
 * Copyright ou © ou Copr. Amaury Carrade (2016)
 * Idées et réflexions : Alexandre Prokopowicz, Amaury Carrade, "Vayan".
 *
 * Ce logiciel est régi par la licence CeCILL soumise au droit français et
 * respectant les principes de diffusion des logiciels libres. Vous pouvez
 * utiliser, modifier et/ou redistribuer ce programme sous les conditions
 * de la licence CeCILL telle que diffusée par le CEA, le CNRS et l'INRIA
 * sur le site "http://www.cecill.info".
 *
 * En contrepartie de l'accessibilité au code source et des droits de copie,
 * de modification et de redistribution accordés par cette licence, il n'est
 * offert aux utilisateurs qu'une garantie limitée.  Pour les mêmes raisons,
 * seule une responsabilité restreinte pèse sur l'auteur du programme,  le
 * titulaire des droits patrimoniaux et les concédants successifs.
 *
 * A cet égard  l'attention de l'utilisateur est attirée sur les risques
 * associés au chargement,  à l'utilisation,  à la modification et/ou au
 * développement et à la reproduction du logiciel par l'utilisateur étant
 * donné sa spécificité de logiciel libre, qui peut le rendre complexe à
 * manipuler et qui le réserve donc à des développeurs et des professionnels
 * avertis possédant  des  connaissances  informatiques approfondies.  Les
 * utilisateurs sont donc invités à charger  et  tester  l'adéquation  du
 * logiciel à leurs besoins dans des conditions permettant d'assurer la
 * sécurité de leurs systèmes et ou de leurs données et, plus généralement,
 * à l'utiliser et l'exploiter dans les mêmes conditions de sécurité.
 *
 * Le fait que vous puissiez accéder à cet en-tête signifie que vous avez
 * pris connaissance de la licence CeCILL, et que vous en avez accepté les
 * termes.
 */
package me.cassayre.florian.hawk;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;
import fr.zcraft.zlib.components.worker.Worker;
import fr.zcraft.zlib.components.worker.WorkerAttributes;
import fr.zcraft.zlib.components.worker.WorkerCallback;
import fr.zcraft.zlib.components.worker.WorkerRunnable;
import fr.zcraft.zlib.tools.Callback;
import me.cassayre.florian.hawk.report.InvalidReportException;
import me.cassayre.florian.hawk.report.Report;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@WorkerAttributes (name = "Reports I/O")
public class ReportsWorker extends Worker
{
    /**
     * Saves a report to the disk.
     *
     * @param report The report to save.
     * @param saveTo The file were this report should be saved. It could be in
     *               non-existent directories: they will be created if needed.
     * @param callbackSuccess Callback on success. Contains the file where the
     *                        report was saved.
     * @param callbackError Callback on error. Contains the exception.
     */
    static void save(final Report report, final File saveTo, final Callback<File> callbackSuccess, final Callback<Throwable> callbackError)
    {
        save(report.toJSON(), saveTo, callbackSuccess, callbackError);
    }

    /**
     * Saves a JSON object to the disk.
     *
     * @param json The JSON object to save.
     * @param saveTo The file were this JSON object should be saved. It could be in
     *               non-existent directories: they will be created if needed.
     * @param callbackSuccess Callback on success. Contains the file where the
     *                        JSON object was saved.
     * @param callbackError Callback on error. Contains the exception.
     */
    static void save(final JsonObject json, final File saveTo, final Callback<File> callbackSuccess, final Callback<Throwable> callbackError)
    {
        submitQuery(new WorkerRunnable<File>()
        {
            @Override
            public File run() throws Throwable
            {
                final File parent = saveTo.getParentFile();
                if (!parent.exists())
                {
                    if (!parent.mkdirs())
                    {
                        throw new IOException("Cannot create directory " + parent.getAbsolutePath());
                    }
                }
                else if (!parent.isDirectory())
                {
                    throw new IOException("The target directory " + parent.getAbsolutePath() + " exist and is not a directory.");
                }

                final FileWriter writer = new FileWriter(saveTo);

                final StringWriter stringWriter = new StringWriter();
                final JsonWriter jsonWriter = new JsonWriter(stringWriter);

                jsonWriter.setLenient(true);
                jsonWriter.setIndent("    ");

                Streams.write(json, jsonWriter);

                writer.write(stringWriter.toString());
                writer.flush();
                writer.close();

                return saveTo;
            }
        }, new WorkerCallback<File>()
        {
            @Override
            public void finished(final File savedTo)
            {
                if (callbackSuccess != null) callbackSuccess.call(savedTo);
            }

            @Override
            public void errored(final Throwable exception)
            {
                if (callbackError != null) callbackError.call(exception);
            }
        });
    }

    /**
     * Publish the given report into a user-friendly web page.
     *
     * @param report The report.
     * @param remoteInstanceURL The remote instance's base URL to send this
     *                          report to.
     * @param callbackSuccess Callback on success, the argument being the
     *                        published report full URL.
     * @param callbackError Callback on error, the argument being the error
     *                      returned, as string.
     */
    static void publish(final Report report, final String remoteInstanceURL, final String userAgent, final Callback<URI> callbackSuccess, final Callback<Throwable> callbackError)
    {
        final JsonObject jsonReport = report.toJSON();

        submitQuery(new WorkerRunnable<URI>()
        {
            @Override
            public URI run() throws Throwable
            {
                try
                {
                    final HTTPResponse response = postJSON(remoteInstanceURL + "/publish", jsonReport.toString(), userAgent);
                    final JsonObject jsonResponse = new Gson().fromJson(response.body, JsonObject.class);

                    if (jsonResponse.has("error"))
                    {
                        throw new InvalidReportException(
                                jsonResponse.has("error_code") ? jsonResponse.get("error_code").getAsString() : null,
                                jsonResponse.has("description") ? jsonResponse.get("description").getAsString() : jsonResponse.get("error").getAsString()
                        );
                    }

                    if (jsonResponse.has("url"))
                    {
                        throw new IOException("The returned JSON value is invalid (missing key `url`).");
                    }

                    try
                    {
                        //return new URI(jsonResponse.get("url").getAsString());
                        return new URI("http://127.0.0.1:8000/K2yc7H57");
                    }
                    catch (URISyntaxException e)
                    {
                        throw new IOException("The returned URL is invalid", e);
                    }
                }
                catch (JsonSyntaxException e)
                {
                    throw new IOException("HTTP request failed: invalid return type.", e);
                }
            }
        }, new WorkerCallback<URI>()
        {
            @Override
            public void finished(URI publishedReportURL)
            {
                if (callbackSuccess != null) callbackSuccess.call(publishedReportURL);
            }

            @Override
            public void errored(Throwable exception)
            {
                if (callbackError != null) callbackError.call(exception);
            }
        });
    }

    /**
     * Makes a POST request, returning the response.
     *
     * Follows the redirections, keeping the POST HTTP method.
     *
     * @param url The URL.
     * @param json The json to send as POST.
     * @param userAgent The User-Agent to present.
     * @return The HTTP response.
     * @throws Throwable In case of I/O error.
     */
    static private HTTPResponse postJSON(final String url, final String json, final String userAgent) throws Throwable
    {
        final URL urlObj = new URL(url);
        final HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("User-Agent", userAgent);
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        connection.setDoOutput(true);

        try
        {
            connection.connect();
            final OutputStream os = connection.getOutputStream();
            os.write(json.getBytes(StandardCharsets.UTF_8));
            os.close();

            int status;
            boolean error = false;

            try
            {
                status = connection.getResponseCode();
            }
            catch (final IOException e)
            {
                // HttpUrlConnection will throw an IOException if any 4XX
                // response is sent. If we request the status again, this
                // time the internal status will be properly set, and we'll be
                // able to retrieve it.
                // Thanks to Iñigo.
                status = connection.getResponseCode();
                error = true;
            }

            if (status >= 400) error = true;

            BufferedReader in = null;
            final String body;
            try
            {
                InputStream stream;
                try
                {
                    stream = connection.getInputStream();
                }
                catch (IOException e)
                {
                    // Same as before
                    stream = connection.getErrorStream();
                    error = true;
                }

                in = new BufferedReader(new InputStreamReader(stream));
                final StringBuilder responseBuilder = new StringBuilder();

                String inputLine;
                while ((inputLine = in.readLine()) != null)
                {
                    responseBuilder.append(inputLine);
                }

                body = responseBuilder.toString();
            }
            finally
            {
                if (in != null) in.close();
            }

            // Is this a redirection?
            switch (status)
            {
                case 301:
                case 302:
                case 307:
                case 308:
                    final String location = connection.getHeaderField("Location");
                    if (location != null)
                    {
                        return postJSON(location, json, userAgent);
                    }
            }

            return new HTTPResponse(body, status, error);
        }
        finally
        {
            connection.disconnect();
        }
    }

    static private class HTTPResponse
    {
        final String body;
        final int status;
        final boolean error;

        HTTPResponse(String body, int status, boolean error)
        {
            this.body = body;
            this.status = status;
            this.error = error;
        }
    }
}
