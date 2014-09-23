import java.util.Scanner;
import java.util.Map;
import java.util.HashMap;
import java.util.Queue;
import java.util.PriorityQueue;
import java.util.LinkedList;
import java.util.Comparator;

import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.nio.IntBuffer;

public class HuffmanCompression {

    public static void main(String[] args) throws FileNotFoundException, IOException {

        if (args.length == 0) {
            
            System.out.println("NO OPERATION GIVEN! OR ANYTHING ELSE FOR THAT MATTER!");
            return;
        }

        HuffmanCompression hc = new HuffmanCompression();

        if (args[0].equals("compress") && args.length == 2) {
            
            System.out.println("Compressing: " + args[1]);
            hc.compressFile(args[1]);
        }
        else if (args[0].equals("decompress") && args.length == 3) {
            
            hc.decompressFile(args[1], args[2]);
        }
    }

    private static final String MAGIC_ENCODING_DELIMITER = ":::DELIMITOR-EXTRORDINAIRE!!!!:::";
    private static final String MAGIC_LINE_ENDER = "::::::::::\n";
    private static final String MAGIC_STRING_OF_COLONS = "::::::::::";

    private class FineBytes {

        private static final int DEFAULT_CAPACITY = 20;

        private byte[] bytes;
        private int bitIndex;
        private int byteIndex;

        public FineBytes() {
            
            bytes = new byte[DEFAULT_CAPACITY];
            for (int i = 0; i < bytes.length; i++) {
            
                bytes[i] = 0x00;
            }
            bitIndex = 0;
            byteIndex = 0;
        }

        public FineBytes(int capacity) {
            
            bytes = new byte[capacity];
            for (int i = 0; i < bytes.length; i++) {
            
                bytes[i] = 0x00;
            }
            bitIndex = 0;
            byteIndex = 0;
        }

        public FineBytes(byte[] bytes) {
            
            this.bytes = bytes;
            bitIndex = 0;
            byteIndex = bytes.length;
        }

        //The string is assumed to have the MSB first, at index 0.
        public FineBytes(String binaryString) {

            bytes = new byte[((int) Math.ceil(binaryString.length() / 8))];

            for (int i = 0; i < bytes.length; i++) {
                
                bytes[i] = 0x00;
            }

            byteIndex = 0;
            bitIndex = 0;

            String byteSizedString;
            for (int i = 0; i < binaryString.length(); i += 8) {

                if (i + 7 < binaryString.length()) {
                    
                    byteSizedString = binaryString.substring(i, i + 8);
                    bytes[byteIndex] = this.parseBinaryString(byteSizedString);
                    byteIndex++;
                }
                else {
                    
                    byteSizedString = binaryString.substring(i, binaryString.length());
                    bytes[byteIndex] = this.parseBinaryString(byteSizedString);
                    bitIndex += byteSizedString.length();
                }
            }
        }

        private void resize() {
            byte[] newBytes = new byte[bytes.length * 2];
            for (int i = 0; i < bytes.length; i++) {
                
                newBytes[i] = bytes[i];
            }
            for (int j = bytes.length; j < newBytes.length; j++) {
                
                newBytes[j] = 0x00;
            }
            bytes = newBytes;
        }

        public void addBytes(byte[] newBytes) {

            if ((byteIndex + newBytes.length) >= bytes.length) {
                
                resize();
            }

            if (this.bitIndex != 0) {
                
                byte bitMask = 0x00;
                switch (this.bitIndex) {
                    case 1:
                        bitMask = 0x7F;
                        break;
                    case 2:
                        bitMask = 0x3F;
                        break;
                    case 3:
                        bitMask = 0x1F;
                        break;
                    case 4:
                        bitMask = 0x0F;
                        break;
                    case 5:
                        bitMask = 0x07;
                        break;
                    case 6:
                        bitMask = 0x03;
                        break;
                    case 7:
                        bitMask = 0x01;
                        break;
                    default:
                        Object o = null;
                        o.toString();
                        break;
                }

                bytes[this.byteIndex] = (byte) ((bytes[this.byteIndex] << (8 - bitIndex)) ^ ((newBytes[0] >>> this.bitIndex) & bitMask));
                this.byteIndex++;
                int newBytesBitIndex = 8 - this.bitIndex;
                this.bitIndex = 0;

                //todo: fix this awful mess....
                int newByteIndex = 0;
                while (newByteIndex < newBytes.length) {

                    if (this.bitIndex != 0) {

                        bitMask = 0x00;
                        switch (this.bitIndex) {
                            case 1:
                                bitMask = 0x7F;
                                break;
                            case 2:
                                bitMask = 0x3F;
                                break;
                            case 3:
                                bitMask = 0x1F;
                                break;
                            case 4:
                                bitMask = 0x0F;
                                break;
                            case 5:
                                bitMask = 0x07;
                                break;
                            case 6:
                                bitMask = 0x03;
                                break;
                            case 7:
                                bitMask = 0x01;
                                break;
                            default:
                                Object o = null;
                                o.toString();
                                break;
                        }

                        bytes[this.byteIndex] = (byte) ((bytes[this.byteIndex] << (8 - bitIndex)) ^ ((newBytes[newByteIndex] >>> this.bitIndex) & bitMask));
                        this.byteIndex++;
                        newBytesBitIndex = 8 - this.bitIndex;
                        this.bitIndex = 0;                        
                    }
                    else {

                        bitMask = 0x00;
                        switch (newBytesBitIndex) {
                            case 1:
                                bitMask = 0x7F;
                                break;
                            case 2:
                                bitMask = 0x3F;
                                break;
                            case 3:
                                bitMask = 0x1F;
                                break;
                            case 4:
                                bitMask = 0x0F;
                                break;
                            case 5:
                                bitMask = 0x07;
                                break;
                            case 6:
                                bitMask = 0x03;
                                break;
                            case 7:
                                bitMask = 0x01;
                                break;
                            default:
                                Object o = null;
                                o.toString();
                                break;
                        }

                        bytes[this.byteIndex] = (byte) ((bytes[this.byteIndex]) ^ (newBytes[newByteIndex] & bitMask));
                        this.bitIndex = 8 - newBytesBitIndex;
                        newByteIndex++;
                    }
                }
            }
            else {

                for (int j = 0; j < newBytes.length; this.byteIndex++, j++) {

                    bytes[this.byteIndex] = newBytes[j];
                }
            }
        }

        public void addBits(String binaryString) {

            //If we need to resize
            if ((byteIndex + binaryString.length()) >= bytes.length) {
                
                resize();
            }

            String byteSizedString;
            if (bitIndex != 0) {

                int substringEndIndex = ((8 - bitIndex) > binaryString.length()) ? binaryString.length() : (8 - bitIndex);
                String bitSmallerString = binaryString.substring(0, substringEndIndex);
                byte tempByte = this.parseBinaryString(bitSmallerString);

                //handle the fucking signed-ness
                if ((bitIndex + substringEndIndex) <= 7) {

                    bytes[byteIndex] = (byte) ((((int) bytes[byteIndex]) << substringEndIndex) ^ tempByte);
                    bitIndex += substringEndIndex;
                }
                else {

                    bytes[byteIndex] = (byte) ((((int) bytes[byteIndex]) << substringEndIndex) ^ tempByte);
                    bitIndex = 0;
                    byteIndex++;
                }
                binaryString = binaryString.substring(substringEndIndex, binaryString.length());

                if (binaryString.length() == 0) {

                    return;
                }
            }

            for (int i = 0; i < binaryString.length(); i += 8) {
                
                if (bitIndex != 0) {

                    int substringEndIndex = ((8 - bitIndex) > binaryString.length()) ? binaryString.length() : (8 - bitIndex);
                    String bitSmallerString = binaryString.substring(0, substringEndIndex);
                    byte tempByte = this.parseBinaryString(bitSmallerString);
                    bytes[byteIndex] = (byte) ((bytes[byteIndex] << substringEndIndex) ^ tempByte);

                    if ((bitIndex + substringEndIndex) <= 7) {

                        bitIndex += substringEndIndex;
                    }
                    else {

                        bitIndex = 0;
                        byteIndex++;
                    }
                }
                else if ((i + 7) < binaryString.length()) {

                    byteSizedString = binaryString.substring(i, i + 8);
                    bytes[byteIndex] = this.parseBinaryString(byteSizedString);
                    byteIndex++;
                }
                else {

                    byteSizedString = binaryString.substring(i, binaryString.length());
                    bytes[byteIndex] = this.parseBinaryString(byteSizedString);
                    bitIndex += byteSizedString.length();
                }
            }
        }

        public byte parseBinaryString(String binaryString) {

            //If we're going to overflow
            if (binaryString.length() == 8 && binaryString.charAt(0) == '1') {

                char[] binaryCharArray = binaryString.toCharArray();
                binaryCharArray[0] = '0';
                binaryString = "-" + new String(binaryCharArray);
            }

            return Byte.parseByte(binaryString, 2);
        }

        public Byte getIncompleteByte() {
            
            if (bitIndex != 0) {
                
                return bytes[byteIndex];
            }
            else {
                
                return null;
            }
        }

        public boolean hasIncompleteByte() {
            
            return (bitIndex != 0);
        }

        public byte[] getCompleteBytes() {

            return (bitIndex != 0) ? copyBackingArray(byteIndex) : copyBackingArray(byteIndex + 1);
        }

        private byte[] copyBackingArray() {

            byte[] copy = new byte[bytes.length];
            for (int i = 0; i < bytes.length; i++) {
                
                copy[i] = bytes[i];
            }

            return copy;
        }

        private byte[] copyBackingArray(int index) {
            
            byte[] copy = new byte[index + 1];

            for (int i = 0; i < (index + 1); i++) {
                
                copy[i] = bytes[i];
            }

            return copy;   
        }

        public byte[] getBytes() {

            int copyIndex = (bitIndex != 0) ? byteIndex : (byteIndex - 1);
            return copyBackingArray(copyIndex);
        }

        public String getBitString(byte b) {

            StringBuilder build = new StringBuilder();

            String byteString;
            byteString = Integer.toBinaryString(b & 0xFF);

            for (int j = 0; j < (8 - byteString.length()); j++) {

                build.append("0");
            }

            build.append(byteString);

            return build.toString();
        }

        public String getBitString(byte[] bytes) {

            StringBuilder build = new StringBuilder();

            String byteString;
            for (int i = 0; i < bytes.length; i++) {
                
                build.append(getBitString(bytes[i]));
            }

            return build.toString();
        }

        public String getBitString() {

            StringBuilder build = new StringBuilder();

            String byteString;
            for (int i = 0; i < byteIndex; i++) {
                
                byteString = Integer.toBinaryString(bytes[i] & 0xFF);

                for (int j = 0; j < (8 - byteString.length()); j++) {

                    build.append("0");
                }

                build.append(byteString);
            }

            if (bitIndex != 0) {

                String binaryString = Integer.toBinaryString(bytes[byteIndex] & 0xFF);

                if (bitIndex == (binaryString.length() % 8)) {

                    build.append(binaryString.substring(0, bitIndex));    
                }
                else {

                    for (int i = 0; i < bitIndex - (binaryString.length() % 8); i++) {

                        build.append("0");
                    }

                    build.append(binaryString);
                }
            }

            return build.toString();
        }

        public String getHexString() {

            StringBuilder build = new StringBuilder();

            String hexString32Bit;
            for (int i = 0; i < byteIndex; i++) {

                hexString32Bit = Integer.toHexString(bytes[i]);
                int beginIndex = (hexString32Bit.length() >= 2) ? (hexString32Bit.length() - 2) : 0;
                if ((hexString32Bit.length() - beginIndex) < 2) {

                    build.append("0");
                }
                build.append(hexString32Bit.substring(beginIndex, hexString32Bit.length()));
            }

            if (bitIndex != 0) {

                hexString32Bit = Integer.toHexString(bytes[byteIndex + 1]);

                int beginIndex = (hexString32Bit.length() >= 2) ? (hexString32Bit.length() - 2) : 0;
                if ((hexString32Bit.length() - beginIndex) < 2) {

                    build.append("0");
                }
                build.append(hexString32Bit.substring(beginIndex, hexString32Bit.length()));
            }

            return build.toString();
        }

        public void clear() {
            
            bytes = new byte[bytes.length];
            bitIndex = 0;
            byteIndex = 0;
        }

        public int numBytes() {
            
            return byteIndex;
        }

        public int numBits() {
            
            return (byteIndex * 8) + bitIndex;
        }
    }

    public void compressFile(String fileName) throws FileNotFoundException, IOException {

        File file = new File(fileName);

        Map<Character, String> encoding = huffmanEncoding(file);

        File compressedFile = new File(fileName + "--compressed");
        writeCompressed(file, compressedFile, encoding);
        readCompressed(fileName + "--compressed", "encoding-file.txt");
    }

    public void writeCompressed(File file, File compressedFile, Map<Character, String> encoding) throws FileNotFoundException, IOException {

        System.out.println("writeCompressed");

        String nextString;
        char c;
        String charEncoding;
        OutputStream os = new FileOutputStream(compressedFile);
        int bitCount = 0;
        long byteCount;
        Scanner countScan = new Scanner(file);
        countScan.useDelimiter("");
        while(countScan.hasNext()) {
            
            nextString = countScan.next();
            c = nextString.charAt(0);
            charEncoding = encoding.get(c);
            bitCount += charEncoding.length();
        }
        countScan.close();
        byteCount = bitCount / 8;
        bitCount %= 8;

        Scanner scan = new Scanner(file);   
        scan.useDelimiter("");

        int totalBytesWritten = 0;
        FineBytes fineBytes = new FineBytes(150);
        ByteBuffer numCharsBuf = ByteBuffer.allocate(12);
        numCharsBuf.putLong(byteCount);
        numCharsBuf.putInt(bitCount);
        os.write(numCharsBuf.array());
        while (scan.hasNext()) {

            nextString = scan.next();
            c = nextString.charAt(0);
            System.out.print(c);
            charEncoding = encoding.get(c);
            fineBytes.addBits(charEncoding);

            //If we have 100 bytes and no incomplete bytes
            if (fineBytes.numBytes() >= 100 && !fineBytes.hasIncompleteByte()) {

                //WRITE!
                System.out.println("WRITE (in middle)");
                System.out.println(fineBytes.getHexString());
                os.write(fineBytes.getBytes());
                totalBytesWritten += fineBytes.numBytes();
                fineBytes.clear();
            }
        }

        System.out.println("WRITE (at end)");
        System.out.println(fineBytes.getHexString());
        os.write(fineBytes.getBytes());
        totalBytesWritten += fineBytes.numBytes();

        os.close();
        scan.close();

        //Write Encoding to file to be retrieved later
        File encodingFile = new File("encoding-file.txt");
        os = new FileOutputStream(encodingFile);
        for (Character myChar : encoding.keySet()) {

            if (myChar.charValue() == '\n') {
                
                os.write("\n".getBytes());
            }
            else {
                
                os.write(myChar.charValue());
            }
            os.write(MAGIC_ENCODING_DELIMITER.getBytes());
            os.write(encoding.get(myChar).getBytes());
            os.write(MAGIC_LINE_ENDER.getBytes());
        }

        os.close();
    }

    private int getInt(ByteBuffer bb, int intIndex) {

        int myInt = 0;
        for (int i = 0; i < 4; i++) {

            myInt = (myInt << (i * 8)) ^ bb.get(i);
        }

        return myInt;
    }

    public void readCompressed(String compressedFileName, String encodingFileName) throws FileNotFoundException, IOException {

        System.out.println("readCompressed Method");
        StringBuilder build = new StringBuilder();

        //Read encoding file
        Map<String, Character> encodingMap = readEncodingFile(encodingFileName);
        int longestEncodingBits = 0;
        int longestEncodingBytes = 0;
        for (String enc : encodingMap.keySet()) {
            
            if (enc.length() > longestEncodingBits) {
            
                longestEncodingBits = enc.length();
            }
        }
        longestEncodingBytes = (longestEncodingBits / 8) + 1;        
        System.out.println("enc:: " + encodingMap);

        //Setup to read from the compressed file
        File compressedFile = new File(compressedFileName);
        InputStream is = new FileInputStream(compressedFile);

        //Three buffers: 
        //numBytesBytes: read the long numBytesBytes value from file
        //numBitsBytes: read the int numBitsBytes from file
        //bytes[]: read the data from the file in a buffer
        byte[] numBytesBytes = new byte[8];
        byte[] numBitsBytes = new byte[4];
        byte[] bytes = new byte[(longestEncodingBytes * 8)];

        //TODO: ALTER SOME CODE SO I CAN HANDLE THE CASE WHERE THE FILE IS VERY LARGE
        //MAKE FineBytes WORK WITH A LONG CAPACITY!
        //Multiple byte arrays!!!!
        FineBytes fineBytes = new FineBytes((int) compressedFile.length());
        String bitString;
        String curEncoding = null;
        char decodedChar;
        
        //Variables to keep track of total bytes and bits in the file
        long totalBytes;
        int totalLeftoverBits;
        long totalBytesRead = 0;
        int totalLeftoverBitsRead = 0;

        //num bytes read per read call
        int bytesRead;

        //Read num bytes
        bytesRead = is.read(numBytesBytes);
        LongBuffer numBytesLongBuffer = ByteBuffer.wrap(numBytesBytes).asLongBuffer();
        totalBytes = numBytesLongBuffer.get();
        numBytesLongBuffer = null;

        //Read num leftover bits
        bytesRead = is.read(numBitsBytes);
        IntBuffer numBitsIntBuffer = ByteBuffer.wrap(numBitsBytes).asIntBuffer();
        totalLeftoverBits = numBitsIntBuffer.get();
        numBitsIntBuffer = null;

        //indices within the binary string
        int encodingStart = -1, prevEncodingEnd = -1, encodingEnd = -1;

        //Read bytes and write them out to string
        while ((bytesRead = is.read(bytes)) != -1) {

            fineBytes.addBytes(bytes);
            bitString = fineBytes.getBitString();

            for (encodingStart = 0, encodingEnd = 1; 
                    encodingEnd <= bitString.length(); 
                    encodingEnd++) {

                curEncoding = bitString.substring(encodingStart, encodingEnd);

                //Check for the current encoding in the mapping
                if (encodingMap.containsKey(curEncoding)) {

                    totalLeftoverBitsRead += curEncoding.length();
                    totalBytesRead += (totalLeftoverBitsRead / 8);
                    totalLeftoverBitsRead %= 8;

                    decodedChar = encodingMap.get(curEncoding);
                    System.out.println((int) decodedChar);

                    if ((int) decodedChar != 13) {

                        build.append(Character.toString(decodedChar));
                    }
                    else {
                        build.append("\n");
                    }
                    System.out.println(build.toString());

                    encodingStart = encodingEnd;
                    prevEncodingEnd = encodingEnd;
                    curEncoding = null;
                    continue;
                }
                else if (totalBytesRead == totalBytes && totalLeftoverBitsRead == totalLeftoverBits) {

                    System.out.println("Decompressed stuff: ||");
                    System.out.println(build.toString());
                    System.out.println("UPPER end readCompressed method");
                    is.close();
                    return;
                }
                else {

                    //break;
                }
            }

            int tempBitsIndex = (prevEncodingEnd == -1) ? 0 : prevEncodingEnd;
            String tempBits = ((curEncoding != null) ? curEncoding : "");
            fineBytes.clear();
            fineBytes.addBits(tempBits);
        }

        is.close();

        System.out.println("Decompressed stuff: ||" + build.toString() + "||");
        System.out.println("LOWER end readCompressed method");
    }

    public void decompressFile(String fileName, String encodingFileName) throws FileNotFoundException {

        //Read encoding file
        Map<String, Character> encodingMap = readEncodingFile(encodingFileName);

        //Read compressed file
        //         and
        //Write decompressed file

    }

    private Map<String, Character> readEncodingFile(String encodingFileName) throws FileNotFoundException {

        File encodingFile = new File(encodingFileName);
        Scanner scan = new Scanner(encodingFile);
        scan.useDelimiter(MAGIC_LINE_ENDER);

        Map<String, Character> encodingMap = new HashMap<String, Character>();
        String encodingLine;
        Character myChar;
        String encoding;
        while(scan.hasNext()) {

            encodingLine = scan.next();

            //This magical character is breaking things here.....it's like carbon monoxide....
            if (encodingLine.indexOf(MAGIC_ENCODING_DELIMITER) == 0) {
                
                continue;
            }

            myChar = encodingLine.split(MAGIC_ENCODING_DELIMITER)[0].charAt(0);
            encoding = encodingLine.split(MAGIC_ENCODING_DELIMITER)[1];
            encoding = encoding.replace(MAGIC_STRING_OF_COLONS, "");
            encodingMap.put(encoding, myChar);
        }
        scan.close();

        return encodingMap;
    }

    //Wrapper class to store data about characters read from file and 
    //also has fields to construct a huffman tree
    class HuffmanNode {
        float freq;
        Character c;
        HuffmanNode left;
        HuffmanNode right;
    }

    public Map<Character, String> huffmanEncoding(File file) throws FileNotFoundException {

        //Read in characters and organize data
        Map<Character, HuffmanNode> initialCharacterMap = new HashMap<Character, HuffmanNode>();
        Scanner scan = new Scanner(file);
        scan.useDelimiter("");
        char c;
        HuffmanNode node;
        int numChars = 0;
        while (scan.hasNext()) {

            c = scan.next().charAt(0);
            numChars++;

            if (initialCharacterMap.containsKey(c)) {
                
                node = initialCharacterMap.get(c);
                node.freq++;
            }
            else {
                
                node = new HuffmanNode();
                node.freq = 1;
                node.c = c;
                node.left = null;
                node.right = null;
                initialCharacterMap.put(c, node);
            }
        }

        //Find the huffman encoding---------------------------------
        //Define PriorityQueue w/Comparator
        PriorityQueue<HuffmanNode> q = new PriorityQueue<HuffmanNode>(initialCharacterMap.size(), 
            new Comparator<HuffmanNode>() {
                public int compare(HuffmanNode n1, HuffmanNode n2) {

                    if (n1.freq < n2.freq) {
                        
                        return -1;
                    }
                    else if (n1.freq > n2.freq) {
                        
                        return 1;
                    }
                    else {
                        
                        return 0;
                    }
                }
        });

        //Divide the counts in each node by the total number of characters
        //and add nodes to the priority queue to prep for huffman tree algorithm
        for (HuffmanNode hn : initialCharacterMap.values()) {

            hn.freq /= numChars;
            q.add(hn);
        }

        //Construct huffman tree
        HuffmanNode innerNode;
        HuffmanNode left;
        HuffmanNode right;
        for (int i = 0; i < (initialCharacterMap.size() - 1); i++) {

            innerNode = new HuffmanNode();
            left = q.poll();
            right = q.poll();
            innerNode.left = left;
            innerNode.right = right;
            innerNode.freq = left.freq + right.freq;
            innerNode.c = null;
            q.add(innerNode);
        }

        //Retrieve the encoding as a mapping from characters to binary strings
        HuffmanNode root = q.poll();
        Map<Character, String> encodingMap = new HashMap<Character, String>();
        getEncodingFromHuffmanTree(root, encodingMap, "");

        return encodingMap;
    }

    private void getEncodingFromHuffmanTree(HuffmanNode root, Map<Character, String> encodingMap, String curEncoding) {

        if (root.c != null) {
            
            encodingMap.put(root.c, curEncoding);
        }
        else {
            
            getEncodingFromHuffmanTree(root.left, encodingMap, curEncoding + "0");
            getEncodingFromHuffmanTree(root.right, encodingMap, curEncoding + "1");
        }
    }
}