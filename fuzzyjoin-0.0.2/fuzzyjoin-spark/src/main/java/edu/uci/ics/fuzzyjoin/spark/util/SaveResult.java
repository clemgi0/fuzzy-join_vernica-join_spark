package edu.uci.ics.fuzzyjoin.spark.util;

import java.io.IOException;
import java.io.File;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import edu.uci.ics.fuzzyjoin.spark.Main;

public class SaveResult {
    private String dataDir;
    private int dataCopy;
    private String dataCopyFormatted;
    private String suffix;
    private Path outputPath;

    public SaveResult(JavaSparkContext sc, String stage) {
        dataDir = sc.getConf().get(Main.DATA_DIR_PROPERTY);

        if (dataDir == null) {
            throw new UnsupportedOperationException(
                    "ERROR: fuzzyjoin.data.dir not set");
        }

        dataCopy = Integer.parseInt(sc.getConf().get(Main.DATA_COPY_PROPERTY, "1"));
        dataCopyFormatted = String.format("-%03d", dataCopy - 1);
        suffix = sc.getConf().get(Main.DATA_SUFFIX_INPUT_PROPERTY, "");

        outputPath = new Path(dataDir + "/" + stage + dataCopyFormatted);
    }

    public static void saveTime(float durationTime, String stage) {
        String timesCsvPath = "times.txt";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(timesCsvPath, true))) {
            // Write the times
            writer.write(Float.toString(durationTime));

            if (stage.equals("fuzzyjoin")) {
                writer.newLine();

                return;
            }
            writer.write(",");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveJavaRDD(JavaRDD<Object> rdd) {
        LogUtil.logStage("Save Java RDD at " + outputPath.toString());

        deleteFile(outputPath);
        rdd.coalesce(1).saveAsTextFile(outputPath.toString());
    }

    public void saveJavaStringRDD(JavaRDD<String> rdd) {
        LogUtil.logStage("Save Java RDD at " + outputPath.toString());

        deleteFile(outputPath);
        rdd.coalesce(1).saveAsTextFile(outputPath.toString());
    }

    private void deleteFile(Path output) {
        // Charger la configuration Hadoop par défaut
        Configuration configuration = new Configuration();

        try {
            // Obtenir l'objet FileSystem pour HDFS
            FileSystem hdfs = FileSystem.get(configuration);

            // Supprimer le fichier ou le répertoire
            boolean isDeleted = hdfs.delete(output, true);

            // Vérifier si la suppression a réussi
            if (isDeleted) {
                LogUtil.logStage("Fichier/répertoire " + output + " supprimé avec succès");
            } else {
                LogUtil.logStage("Échec de la suppression du fichier/répertoire " + output);
            }

            // Fermer l'objet FileSystem
            hdfs.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
