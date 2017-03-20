import java.io.*;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class Unwrap {

    public static final int[] SUBSTITUTION_TABLE = {0x3d, 0x65, 0x85, 0xb3,
            0x18, 0xdb, 0xe2, 0x87, 0xf1, 0x52, 0xab, 0x63, 0x4b, 0xb5, 0xa0,
            0x5f, 0x7d, 0x68, 0x7b, 0x9b, 0x24, 0xc2, 0x28, 0x67, 0x8a, 0xde,
            0xa4, 0x26, 0x1e, 0x03, 0xeb, 0x17, 0x6f, 0x34, 0x3e, 0x7a, 0x3f,
            0xd2, 0xa9, 0x6a, 0x0f, 0xe9, 0x35, 0x56, 0x1f, 0xb1, 0x4d, 0x10,
            0x78, 0xd9, 0x75, 0xf6, 0xbc, 0x41, 0x04, 0x81, 0x61, 0x06, 0xf9,
            0xad, 0xd6, 0xd5, 0x29, 0x7e, 0x86, 0x9e, 0x79, 0xe5, 0x05, 0xba,
            0x84, 0xcc, 0x6e, 0x27, 0x8e, 0xb0, 0x5d, 0xa8, 0xf3, 0x9f, 0xd0,
            0xa2, 0x71, 0xb8, 0x58, 0xdd, 0x2c, 0x38, 0x99, 0x4c, 0x48, 0x07,
            0x55, 0xe4, 0x53, 0x8c, 0x46, 0xb6, 0x2d, 0xa5, 0xaf, 0x32, 0x22,
            0x40, 0xdc, 0x50, 0xc3, 0xa1, 0x25, 0x8b, 0x9c, 0x16, 0x60, 0x5c,
            0xcf, 0xfd, 0x0c, 0x98, 0x1c, 0xd4, 0x37, 0x6d, 0x3c, 0x3a, 0x30,
            0xe8, 0x6c, 0x31, 0x47, 0xf5, 0x33, 0xda, 0x43, 0xc8, 0xe3, 0x5e,
            0x19, 0x94, 0xec, 0xe6, 0xa3, 0x95, 0x14, 0xe0, 0x9d, 0x64, 0xfa,
            0x59, 0x15, 0xc5, 0x2f, 0xca, 0xbb, 0x0b, 0xdf, 0xf2, 0x97, 0xbf,
            0x0a, 0x76, 0xb4, 0x49, 0x44, 0x5a, 0x1d, 0xf0, 0x00, 0x96, 0x21,
            0x80, 0x7f, 0x1a, 0x82, 0x39, 0x4f, 0xc1, 0xa7, 0xd7, 0x0d, 0xd1,
            0xd8, 0xff, 0x13, 0x93, 0x70, 0xee, 0x5b, 0xef, 0xbe, 0x09, 0xb9,
            0x77, 0x72, 0xe7, 0xb2, 0x54, 0xb7, 0x2a, 0xc7, 0x73, 0x90, 0x66,
            0x20, 0x0e, 0x51, 0xed, 0xf8, 0x7c, 0x8f, 0x2e, 0xf4, 0x12, 0xc6,
            0x2b, 0x83, 0xcd, 0xac, 0xcb, 0x3b, 0xc4, 0x4e, 0xc0, 0x69, 0x36,
            0x62, 0x02, 0xae, 0x88, 0xfc, 0xaa, 0x42, 0x08, 0xa6, 0x45, 0x57,
            0xd3, 0x9a, 0xbd, 0xe1, 0x23, 0x8d, 0x92, 0x4a, 0x11, 0x89, 0x74,
            0x6b, 0x91, 0xfb, 0xfe, 0xc9, 0x01, 0xea, 0x1b, 0xf7, 0xce};

    private static void deflate(byte[] decodedBuffer, File file)
            throws DataFormatException, IOException {

        int bufferLength = 512;
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
        Inflater decompresser = new Inflater();
        decompresser.setInput(decodedBuffer, 0, decodedBuffer.length);
        byte[] buffer = new byte[bufferLength];
        int resultLength = decompresser.inflate(buffer);

        while (resultLength != 0) {
            bos.write(buffer, 0, resultLength);
            resultLength = decompresser.inflate(buffer);
        }

        decompresser.end();
        bos.close();
    }

    private static byte[] decode(byte[] codedBuffer) {
        int SHAHashLength = 20;
        byte[] decodedBuffer = new byte[codedBuffer.length - SHAHashLength];

        for (int i = SHAHashLength; i < codedBuffer.length; i++) {
            int decodedByte = SUBSTITUTION_TABLE[(codedBuffer[i] & 0xFF)];
            decodedBuffer[i - SHAHashLength] = (byte) decodedByte;
        }

        return decodedBuffer;
    }

    private static String getDataToDecode(BufferedReader br, int dataToDecodeLength) throws IOException {
        StringBuffer dataToDecodeBuffer = new StringBuffer(dataToDecodeLength);
        char c = (char) br.read();

        while (dataToDecodeBuffer.length() < dataToDecodeLength) {
            dataToDecodeBuffer.append(c);
            c = (char) br.read();
        }

        String strDataToDecode = dataToDecodeBuffer.toString();
        strDataToDecode = strDataToDecode.replaceAll("\n", "");
        return strDataToDecode;
    }

    public static void decodeFile(File inFile, File outFile) throws IOException, DataFormatException {

        String content = FileParser.loadFileIntoMemory(inFile);
        BufferedReader br = new BufferedReader(new StringReader(content));
        String line = null;
        boolean sizeFound = false;

        do {
            line = br.readLine();
            sizeFound = line.matches("^[0-9a-f]+ ([0-9a-f]+)$");
        } while ((line != null) && !sizeFound);

        if (sizeFound) {
            int dataToDecodeLength = Integer.parseInt(line.split(" ")[1], 16);
            String dataToDecode = getDataToDecode(br, dataToDecodeLength);
            byte[] base64Str = Base64Decoder.decodeBuffer(dataToDecode);
            byte[] decodedBase64Str = decode(base64Str);
            deflate(decodedBase64Str, outFile);
        } else {
            System.err
                    .println("==> Erreur: Le fichier d'entrée n'est pas au bon format");
        }
        br.close();
    }

    public static void decodeDir(File inDir, File outDir) throws IOException, DataFormatException {
        File[] files = inDir.listFiles(new FilenameFilter() {
            public boolean accept(File arg0, String arg1) {
                return arg1.toLowerCase().endsWith(".plb");
            }
        });

        if (!outDir.mkdir()) {
            System.err.println("Erreur ==> Impossible de créer ce dossier: " + inDir.getAbsolutePath());

        } else {

            for (int i = 0; i < files.length; i++) {
                File inFile = files[i];
                System.out.println("Décryptage du fichier ==> " + inFile.getName());
                String newFileName = inFile.getName().replaceAll(".plb", ".sql");
                File outFile = new File(outDir + File.separator + newFileName);
                decodeFile(inFile, outFile);
            }

        }
    }

}
