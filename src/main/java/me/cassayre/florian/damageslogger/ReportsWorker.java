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
package me.cassayre.florian.damageslogger;

import com.google.gson.JsonObject;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;
import fr.zcraft.zlib.components.worker.Worker;
import fr.zcraft.zlib.components.worker.WorkerAttributes;
import fr.zcraft.zlib.components.worker.WorkerCallback;
import fr.zcraft.zlib.components.worker.WorkerRunnable;
import fr.zcraft.zlib.tools.Callback;
import me.cassayre.florian.damageslogger.report.Report;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;

@WorkerAttributes (name = "Reports I/O")
public class ReportsWorker extends Worker
{
    public static void save(final Report report, final File saveTo, final Callback<File> callbackSuccess, final Callback<Throwable> callbackError)
    {
        final JsonObject jsonReport = report.toJSON();

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

                Streams.write(jsonReport, jsonWriter);

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
}
