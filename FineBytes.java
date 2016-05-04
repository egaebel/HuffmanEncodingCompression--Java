/*
* The MIT License (MIT)
* Copyright (c) 2016 Ethan Gaebel
* 
* Permission is hereby granted, free of charge, to any person obtaining a copy of this software 
* and associated documentation files (the "Software"), to deal in the Software without restriction, 
* including without limitation the rights to use, copy, modify, merge, publish, distribute, 
* sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is 
* furnished to do so, subject to the following conditions:
* 
* The above copyright notice and this permission notice shall be included in all copies or 
* substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING 
* BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND 
* NONINFRINGEMENT. 
* IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN 
* CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

import java.lang.IllegalStateException;

/**
 * Class to support bit-level byte manipulation in an array of bytes.
 * This array of bytes can be accessed by a byte index and a bit index. 
 */
public class FineBytes {

    //~Constants-----------------------------------
    private static final int DEFAULT_CAPACITY = 20;

    //~Fields--------------------------------------
    /**
     * The array of bytes backing FineBytes.
     */
    private byte[] bytes;

    /**
     * The index of the bit in the final byte of the bytes array.
     */
    private int bitIndex;

    /**
     * The index of the last (possibly incomplete) byte.
     */
    private int byteIndex;

    /**
     * Set up the byte array with the DEFAULT_CAPACITY (20) and 0 out everything.
     */
    public FineBytes() {
        
        bytes = new byte[DEFAULT_CAPACITY];
        for (int i = 0; i < bytes.length; i++) {
        
            bytes[i] = 0x00;
        }
        bitIndex = 0;
        byteIndex = 0;
    }

    /**
     * Set up the byte array with the passed in capacity and 0 out everything.
     */
    public FineBytes(int capacity) {
        
        bytes = new byte[capacity];
        for (int i = 0; i < bytes.length; i++) {
        
            bytes[i] = 0x00;
        }
        bitIndex = 0;
        byteIndex = 0;
    }

    /**
     * Set up the byte array with the passed in byte array.
     */
    public FineBytes(byte[] bytes) {
        
        this.bytes = bytes;
        bitIndex = 0;
        byteIndex = bytes.length;
    }

    /**
     * Set up the byte array using a string of binary digits. 
     */
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
            } else {
                
                byteSizedString = binaryString.substring(i, binaryString.length());
                bytes[byteIndex] = this.parseBinaryString(byteSizedString);
                bitIndex += byteSizedString.length();
            }
        }
    }

    /**
     * Double the size of the byte array.
     */
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

    /**
     * Add an array of bytes to the end.
     */
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
                    throw new IllegalStateException("");
            }

            bytes[this.byteIndex] = (byte) ((bytes[this.byteIndex] << (8 - bitIndex)) 
                    ^ ((newBytes[0] >>> this.bitIndex) & bitMask));
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
                            throw new IllegalStateException("");
                    }

                    bytes[this.byteIndex] = (byte) ((bytes[this.byteIndex] << (8 - bitIndex)) 
                            ^ ((newBytes[newByteIndex] >>> this.bitIndex) & bitMask));
                    this.byteIndex++;
                    newBytesBitIndex = 8 - this.bitIndex;
                    this.bitIndex = 0;                        
                } else {

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
                            throw new IllegalStateException("");
                    }

                    bytes[this.byteIndex] = (byte) ((bytes[this.byteIndex]) 
                            ^ (newBytes[newByteIndex] & bitMask));
                    this.bitIndex = 8 - newBytesBitIndex;
                    newByteIndex++;
                }
            }
        } else {

            for (int j = 0; j < newBytes.length; this.byteIndex++, j++) {

                bytes[this.byteIndex] = newBytes[j];
            }
        }
    }

    /**
     * Add a string of bits to the end.
     */
    public void addBits(String binaryString) {

        //If we need to resize
        if ((byteIndex + binaryString.length()) >= bytes.length) {
            
            resize();
        }

        String byteSizedString;
        if (bitIndex != 0) {

            int substringEndIndex = ((8 - bitIndex) > binaryString.length()) 
                    ? binaryString.length() : (8 - bitIndex);
            String bitSmallerString = binaryString.substring(0, substringEndIndex);
            byte tempByte = this.parseBinaryString(bitSmallerString);

            //handle the fucking signed-ness
            if ((bitIndex + substringEndIndex) <= 7) {

                bytes[byteIndex] = (byte) ((((int) bytes[byteIndex]) << substringEndIndex) 
                        ^ tempByte);
                bitIndex += substringEndIndex;
            } else {

                bytes[byteIndex] = (byte) ((((int) bytes[byteIndex]) << substringEndIndex) 
                        ^ tempByte);
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

                int substringEndIndex = ((8 - bitIndex) > binaryString.length()) 
                        ? binaryString.length() : (8 - bitIndex);
                String bitSmallerString = binaryString.substring(0, substringEndIndex);
                byte tempByte = this.parseBinaryString(bitSmallerString);
                bytes[byteIndex] = (byte) ((bytes[byteIndex] << substringEndIndex) ^ tempByte);

                if ((bitIndex + substringEndIndex) <= 7) {

                    bitIndex += substringEndIndex;
                } else {

                    bitIndex = 0;
                    byteIndex++;
                }
            } else if ((i + 7) < binaryString.length()) {

                byteSizedString = binaryString.substring(i, i + 8);
                bytes[byteIndex] = this.parseBinaryString(byteSizedString);
                byteIndex++;
            } else {

                byteSizedString = binaryString.substring(i, binaryString.length());
                bytes[byteIndex] = this.parseBinaryString(byteSizedString);
                bitIndex += byteSizedString.length();
            }
        }
    }

    /**
     * Parse a string of binary digits into bytes.
     */
    public static byte parseBinaryString(String binaryString) {

        //If we're going to overflow
        if (binaryString.length() == 8 && binaryString.charAt(0) == '1') {

            char[] binaryCharArray = binaryString.toCharArray();
            binaryCharArray[0] = '0';
            binaryString = "-" + new String(binaryCharArray);
        }

        return Byte.parseByte(binaryString, 2);
    }

    /**
     * Get the byte at the end of the array which is not fully used.
     */
    public Byte getIncompleteByte() {
        
        if (bitIndex != 0) {
            
            return bytes[byteIndex];
        } else {
            
            return null;
        }
    }

    /**
     * Check if there is a byte at the end of the array which is not fully used.
     */
    public boolean hasIncompleteByte() {
        
        return (bitIndex != 0);
    }

    /**
     * Get all the bytes backing this object.
     * The bytes are copied and returned.
     */
    public byte[] getCompleteBytes() {

        return (bitIndex != 0) ? copyBackingArray(byteIndex) : copyBackingArray(byteIndex + 1);
    }

    /**
     * Copy the backing array of this object.
     */
    private byte[] copyBackingArray() {

        byte[] copy = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            
            copy[i] = bytes[i];
        }

        return copy;
    }

    /**
     * Copy the backing array of this object up until index.
     * Index is exclusive.
     */
    private byte[] copyBackingArray(int index) {
        
        byte[] copy = new byte[index + 1];

        for (int i = 0; i < (index + 1); i++) {
            
            copy[i] = bytes[i];
        }

        return copy;   
    }

    /**
     * Copy the backing array of this object from beginIndex up until endIndex.
     * beginIndex is inclusive.
     * endIndex is exclusive.
     * 
     * @param beginIndex the index to begin copying at.
     * @param endIndex the index to stop copying at.
     * @return The copied section of bytes in this object.
     */
    private byte[] copyBackingArray(int beginIndex, int endIndex) {
        
        byte[] copy = new byte[endIndex + 1];

        for (int i = 0; i < (endIndex + 1); i++) {
            
            copy[i] = bytes[beginIndex + i];
        }

        return copy;   
    }

    /**
     * Get all the fully filled in bytes. Excluding any partially filled in bytes.
     */
    public byte[] getBytes() {

        int copyIndex = (bitIndex != 0) ? byteIndex : (byteIndex - 1);
        return copyBackingArray(copyIndex);
    }

    /**
     * Get the last numBytes from the FineBytes array.
     * The last numBytes begins at the last valid bit index.
     *
     * @param numBytes the number of bytes to get from the byte array.
     * @return the final numBytes in the byte array.
     */
    public byte[] getLastBytes(int numBytes) {
        if (bitIndex == 0) {
            return copyBackingArray((bytes.length - numBytes), bytes.length);
        }

        int beginByteIndex = (bytes.length - numBytes);
        byte[] copy = new byte[numBytes];
        for (int i = 0; i <= numBytes; i++) {

            if (numBytes > i && i > 0) {
                copy[i - 1] = (byte) (((int) copy[i]) 
                        ^ (((int) bytes[beginByteIndex]) >> (8 - bitIndex)));
            }
            copy[i] = (byte) (((int) bytes[beginByteIndex]) << bitIndex); 
        }
        return copy;
    }

    /**
     * Get the bit string of a passed byte.
     */
    public static String getBitString(byte b) {

        StringBuilder build = new StringBuilder();

        String byteString;
        byteString = Integer.toBinaryString(b & 0xFF);

        for (int j = 0; j < (8 - byteString.length()); j++) {

            build.append("0");
        }

        build.append(byteString);

        return build.toString();
    }

    /**
     * Get the bit string of a passed byte array.
     */
    public static String getBitString(byte[] bytes) {

        StringBuilder build = new StringBuilder();

        String byteString;
        for (int i = 0; i < bytes.length; i++) {
            
            build.append(getBitString(bytes[i]));
        }

        return build.toString();
    }

    /**
     * Get the bit string of this FineBytes object.
     */
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
            } else {

                for (int i = 0; i < bitIndex - (binaryString.length() % 8); i++) {

                    build.append("0");
                }

                build.append(binaryString);
            }
        }

        return build.toString();
    }

    /**
     * Get a hex string of the bytes in this FineBytes object.
     */
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

    /**
     * Clear out the bytes and bits in this object.
     */
    public void clear() {
        
        bytes = new byte[bytes.length];
        bitIndex = 0;
        byteIndex = 0;
    }

    /**
     * Get the number of bytes in this object (including partials).
     */
    public int numBytes() {
        
        return byteIndex;
    }

    /**
     * Get the total number of bits in this object.
     */
    public int numBits() {
        
        return (byteIndex * 8) + bitIndex;
    }
}