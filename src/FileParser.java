import java.io.*;
import java.util.regex.Pattern;

public class FileParser {

    private final static String LINE_SEPARATOR = System.getProperty("line.separator");

    private final static String HEADER_START = "CREATE OR REPLACE";

    private final static String HEADER_END = "WRAPPED";

    public static String loadFileIntoMemory(File file) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(file));
        String content = "";
        String line = br.readLine();
        while (line != null) {
            content += line + "\n";
            line = br.readLine();
        }
        br.close();
        return content;
    }

    public static String getFileName(String header) {
        String buff = header.toLowerCase();
        buff = Pattern.compile("/\\*.*\\*/", Pattern.DOTALL).matcher(buff).replaceAll(" ");
        buff = Pattern.compile("--[^\n]*\n", Pattern.DOTALL).matcher(buff).replaceAll(" ");
        buff = buff.replaceAll("\\s{2,}", " ");
        buff = buff.trim();
        buff = buff.substring(HEADER_START.length() + 1, buff.length() - (HEADER_END + " ").length());
        String[] words = buff.split(" ");
        String fileName = "";
        for (int i = 0; i < words.length - 1; i++) {
            fileName += words[i].charAt(0);
        }
        fileName += "_";
        fileName += words[words.length - 1];
        fileName += ".plb";
        return fileName;
    }

    public static File cutFile(File file) throws IOException {
        String dirName = file.getAbsolutePath().replaceAll(".plb", "_plb");
        File dir = new File(dirName);
        if (dir.exists()) {
            System.err.println("Erreur ==> Un dossier portant ce nom existe déjà: " + dir.getAbsolutePath());

        } else {
            if (!dir.mkdir()) {
                System.err.println("Erreur ==> Impossible de créer ce dossier: " + dir.getAbsolutePath());
            } else {
                boolean betweenHeaderAndFooter = false;
                BufferedWriter bw = null;
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line = br.readLine();
                while (line != null) {
                    if (line.toUpperCase().matches(HEADER_START + " .*")) {
                        betweenHeaderAndFooter = true;
                        String buffer = line;
                        while (!(buffer.toUpperCase().contains(HEADER_END)) && (line != null)) {
                            line = br.readLine();
                            buffer += LINE_SEPARATOR + line;
                        }
                        String fileName = getFileName(buffer);
                        System.out.println("Fichier crypté trouvé ==> " + fileName);
                        bw = new BufferedWriter(new FileWriter(dir.getAbsolutePath() + File.separator + fileName));
                        bw.write(buffer + LINE_SEPARATOR);
                    } else if (betweenHeaderAndFooter) {
                        bw.write(line + LINE_SEPARATOR);
                    }
                    if (line.matches("/") && betweenHeaderAndFooter) {
                        betweenHeaderAndFooter = false;
                        bw.close();
                    }
                    line = br.readLine();
                }
                br.close();
            }
        }
        return dir;
    }

    public static void main(String[] args) throws Exception {
        try {

            if (args.length == 1) {

                File in = new File(args[0]);
                File out = new File(args[0] + ".decrypt");

                if (in.isDirectory()) {
                    Unwrap.decodeDir(in, out);
                } else {
                    Unwrap.decodeFile(in, out);
                }

            } else {
                throw new IllegalArgumentException("Vous devez spécifiez le chemin du fichier ou du dossier à " +
                        "décrypter en paramètre");

            }

        } catch (Exception e) {
            System.err.println("Erreur: Une erreur est survenue pendant l'exécution de l'application: ");
            e.printStackTrace();
            System.exit(-1);

        }

    }
}
