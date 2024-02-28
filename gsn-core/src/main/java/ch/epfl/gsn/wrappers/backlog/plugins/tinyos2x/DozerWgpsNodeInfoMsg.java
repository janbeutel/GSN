/**
 * This class is automatically generated by mig. DO NOT EDIT THIS FILE.
 * This class implements a Java interface to the 'DozerWgpsNodeInfoMsg'
 * message type.
 */

 package ch.epfl.gsn.wrappers.backlog.plugins.tinyos2x;

public class DozerWgpsNodeInfoMsg extends DataHeaderMsg {

    /** The default size of this message type in bytes. */
    public static final int DEFAULT_MESSAGE_SIZE = 28;

    /** The Active Message type associated with this message. */
    public static final int AM_TYPE = 214;

    /** Create a new DozerWgpsNodeInfoMsg of size 28. */
    public DozerWgpsNodeInfoMsg() {
        super(DEFAULT_MESSAGE_SIZE);
        amTypeSet(AM_TYPE);
    }

    /** Create a new DozerWgpsNodeInfoMsg of the given data_length. */
    public DozerWgpsNodeInfoMsg(int data_length) {
        super(data_length);
        amTypeSet(AM_TYPE);
    }

    /**
     * Create a new DozerWgpsNodeInfoMsg with the given data_length
     * and base offset.
     */
    public DozerWgpsNodeInfoMsg(int data_length, int base_offset) {
        super(data_length, base_offset);
        amTypeSet(AM_TYPE);
    }

    /**
     * Create a new DozerWgpsNodeInfoMsg using the given byte array
     * as backing store.
     */
    public DozerWgpsNodeInfoMsg(byte[] data) {
        super(data);
        amTypeSet(AM_TYPE);
    }

    /**
     * Create a new DozerWgpsNodeInfoMsg using the given byte array
     * as backing store, with the given base offset.
     */
    public DozerWgpsNodeInfoMsg(byte[] data, int base_offset) {
        super(data, base_offset);
        amTypeSet(AM_TYPE);
    }

    /**
     * Create a new DozerWgpsNodeInfoMsg using the given byte array
     * as backing store, with the given base offset and data length.
     */
    public DozerWgpsNodeInfoMsg(byte[] data, int base_offset, int data_length) {
        super(data, base_offset, data_length);
        amTypeSet(AM_TYPE);
    }

    /**
     * Create a new DozerWgpsNodeInfoMsg embedded in the given message
     * at the given base offset.
     */
    public DozerWgpsNodeInfoMsg(net.tinyos.message.Message msg, int base_offset) {
        super(msg, base_offset, DEFAULT_MESSAGE_SIZE);
        amTypeSet(AM_TYPE);
    }

    /**
     * Create a new DozerWgpsNodeInfoMsg embedded in the given message
     * at the given base offset and length.
     */
    public DozerWgpsNodeInfoMsg(net.tinyos.message.Message msg, int base_offset, int data_length) {
        super(msg, base_offset, data_length);
        amTypeSet(AM_TYPE);
    }

    /**
    /* Return a String representation of this message. Includes the
     * message type name and the non-indexed field values.
     */
    public String toString() {
      String s = "Message <DozerWgpsNodeInfoMsg> \n";
      try {
        s += "  [header.seqNr=0x"+Long.toHexString(get_header_seqNr())+"]\n";
      } catch (ArrayIndexOutOfBoundsException aioobe) { /* Skip field */ }
      try {
        s += "  [header.originatorID=0x"+Long.toHexString(get_header_originatorID())+"]\n";
      } catch (ArrayIndexOutOfBoundsException aioobe) { /* Skip field */ }
      try {
        s += "  [header.aTime.low=0x"+Long.toHexString(get_header_aTime_low())+"]\n";
      } catch (ArrayIndexOutOfBoundsException aioobe) { /* Skip field */ }
      try {
        s += "  [header.aTime.high=0x"+Long.toHexString(get_header_aTime_high())+"]\n";
      } catch (ArrayIndexOutOfBoundsException aioobe) { /* Skip field */ }
      try {
        s += "  [payload.rst_cnt=0x"+Long.toHexString(get_payload_rst_cnt())+"]\n";
      } catch (ArrayIndexOutOfBoundsException aioobe) { /* Skip field */ }
      try {
        s += "  [payload.rst_flag=0x"+Long.toHexString(get_payload_rst_flag())+"]\n";
      } catch (ArrayIndexOutOfBoundsException aioobe) { /* Skip field */ }
      try {
        s += "  [payload.compiler_desc1=0x"+Long.toHexString(get_payload_compiler_desc1())+"]\n";
      } catch (ArrayIndexOutOfBoundsException aioobe) { /* Skip field */ }
      try {
        s += "  [payload.compiler_desc2=0x"+Long.toHexString(get_payload_compiler_desc2())+"]\n";
      } catch (ArrayIndexOutOfBoundsException aioobe) { /* Skip field */ }
      try {
        s += "  [payload.compiler_desc3=0x"+Long.toHexString(get_payload_compiler_desc3())+"]\n";
      } catch (ArrayIndexOutOfBoundsException aioobe) { /* Skip field */ }
      try {
        s += "  [payload.compiler_desc4=0x"+Long.toHexString(get_payload_compiler_desc4())+"]\n";
      } catch (ArrayIndexOutOfBoundsException aioobe) { /* Skip field */ }
      try {
        s += "  [payload.compiler_ver=0x"+Long.toHexString(get_payload_compiler_ver())+"]\n";
      } catch (ArrayIndexOutOfBoundsException aioobe) { /* Skip field */ }
      try {
        s += "  [payload.compile_date=0x"+Long.toHexString(get_payload_compile_date())+"]\n";
      } catch (ArrayIndexOutOfBoundsException aioobe) { /* Skip field */ }
      try {
        s += "  [payload.fw_ver=0x"+Long.toHexString(get_payload_fw_ver())+"]\n";
      } catch (ArrayIndexOutOfBoundsException aioobe) { /* Skip field */ }
      try {
        s += "  [payload.sw_rev_id=0x"+Long.toHexString(get_payload_sw_rev_id())+"]\n";
      } catch (ArrayIndexOutOfBoundsException aioobe) { /* Skip field */ }
      return s;
    }

    // Message-type-specific access methods appear below.

    /////////////////////////////////////////////////////////
    // Accessor methods for field: header.seqNr
    //   Field type: int
    //   Offset (bits): 0
    //   Size (bits): 16
    /////////////////////////////////////////////////////////

    /**
     * Return whether the field 'header.seqNr' is signed (false).
     */
    public static boolean isSigned_header_seqNr() {
        return false;
    }

    /**
     * Return whether the field 'header.seqNr' is an array (false).
     */
    public static boolean isArray_header_seqNr() {
        return false;
    }

    /**
     * Return the offset (in bytes) of the field 'header.seqNr'
     */
    public static int offset_header_seqNr() {
        return (0 / 8);
    }

    /**
     * Return the offset (in bits) of the field 'header.seqNr'
     */
    public static int offsetBits_header_seqNr() {
        return 0;
    }

    /**
     * Return the value (as a int) of the field 'header.seqNr'
     */
    public int get_header_seqNr() {
        return (int)getUIntBEElement(offsetBits_header_seqNr(), 16);
    }

    /**
     * Set the value of the field 'header.seqNr'
     */
    public void set_header_seqNr(int value) {
        setUIntBEElement(offsetBits_header_seqNr(), 16, value);
    }

    /**
     * Return the size, in bytes, of the field 'header.seqNr'
     */
    public static int size_header_seqNr() {
        return (16 / 8);
    }

    /**
     * Return the size, in bits, of the field 'header.seqNr'
     */
    public static int sizeBits_header_seqNr() {
        return 16;
    }

    /////////////////////////////////////////////////////////
    // Accessor methods for field: header.originatorID
    //   Field type: int
    //   Offset (bits): 16
    //   Size (bits): 16
    /////////////////////////////////////////////////////////

    /**
     * Return whether the field 'header.originatorID' is signed (false).
     */
    public static boolean isSigned_header_originatorID() {
        return false;
    }

    /**
     * Return whether the field 'header.originatorID' is an array (false).
     */
    public static boolean isArray_header_originatorID() {
        return false;
    }

    /**
     * Return the offset (in bytes) of the field 'header.originatorID'
     */
    public static int offset_header_originatorID() {
        return (16 / 8);
    }

    /**
     * Return the offset (in bits) of the field 'header.originatorID'
     */
    public static int offsetBits_header_originatorID() {
        return 16;
    }

    /**
     * Return the value (as a int) of the field 'header.originatorID'
     */
    public int get_header_originatorID() {
        return (int)getUIntBEElement(offsetBits_header_originatorID(), 16);
    }

    /**
     * Set the value of the field 'header.originatorID'
     */
    public void set_header_originatorID(int value) {
        setUIntBEElement(offsetBits_header_originatorID(), 16, value);
    }

    /**
     * Return the size, in bytes, of the field 'header.originatorID'
     */
    public static int size_header_originatorID() {
        return (16 / 8);
    }

    /**
     * Return the size, in bits, of the field 'header.originatorID'
     */
    public static int sizeBits_header_originatorID() {
        return 16;
    }

    /////////////////////////////////////////////////////////
    // Accessor methods for field: header.aTime.low
    //   Field type: int
    //   Offset (bits): 32
    //   Size (bits): 16
    /////////////////////////////////////////////////////////

    /**
     * Return whether the field 'header.aTime.low' is signed (false).
     */
    public static boolean isSigned_header_aTime_low() {
        return false;
    }

    /**
     * Return whether the field 'header.aTime.low' is an array (false).
     */
    public static boolean isArray_header_aTime_low() {
        return false;
    }

    /**
     * Return the offset (in bytes) of the field 'header.aTime.low'
     */
    public static int offset_header_aTime_low() {
        return (32 / 8);
    }

    /**
     * Return the offset (in bits) of the field 'header.aTime.low'
     */
    public static int offsetBits_header_aTime_low() {
        return 32;
    }

    /**
     * Return the value (as a int) of the field 'header.aTime.low'
     */
    public int get_header_aTime_low() {
        return (int)getUIntBEElement(offsetBits_header_aTime_low(), 16);
    }

    /**
     * Set the value of the field 'header.aTime.low'
     */
    public void set_header_aTime_low(int value) {
        setUIntBEElement(offsetBits_header_aTime_low(), 16, value);
    }

    /**
     * Return the size, in bytes, of the field 'header.aTime.low'
     */
    public static int size_header_aTime_low() {
        return (16 / 8);
    }

    /**
     * Return the size, in bits, of the field 'header.aTime.low'
     */
    public static int sizeBits_header_aTime_low() {
        return 16;
    }

    /////////////////////////////////////////////////////////
    // Accessor methods for field: header.aTime.high
    //   Field type: short
    //   Offset (bits): 48
    //   Size (bits): 8
    /////////////////////////////////////////////////////////

    /**
     * Return whether the field 'header.aTime.high' is signed (false).
     */
    public static boolean isSigned_header_aTime_high() {
        return false;
    }

    /**
     * Return whether the field 'header.aTime.high' is an array (false).
     */
    public static boolean isArray_header_aTime_high() {
        return false;
    }

    /**
     * Return the offset (in bytes) of the field 'header.aTime.high'
     */
    public static int offset_header_aTime_high() {
        return (48 / 8);
    }

    /**
     * Return the offset (in bits) of the field 'header.aTime.high'
     */
    public static int offsetBits_header_aTime_high() {
        return 48;
    }

    /**
     * Return the value (as a short) of the field 'header.aTime.high'
     */
    public short get_header_aTime_high() {
        return (short)getUIntBEElement(offsetBits_header_aTime_high(), 8);
    }

    /**
     * Set the value of the field 'header.aTime.high'
     */
    public void set_header_aTime_high(short value) {
        setUIntBEElement(offsetBits_header_aTime_high(), 8, value);
    }

    /**
     * Return the size, in bytes, of the field 'header.aTime.high'
     */
    public static int size_header_aTime_high() {
        return (8 / 8);
    }

    /**
     * Return the size, in bits, of the field 'header.aTime.high'
     */
    public static int sizeBits_header_aTime_high() {
        return 8;
    }

    /////////////////////////////////////////////////////////
    // Accessor methods for field: payload.rst_cnt
    //   Field type: int
    //   Offset (bits): 56
    //   Size (bits): 16
    /////////////////////////////////////////////////////////

    /**
     * Return whether the field 'payload.rst_cnt' is signed (false).
     */
    public static boolean isSigned_payload_rst_cnt() {
        return false;
    }

    /**
     * Return whether the field 'payload.rst_cnt' is an array (false).
     */
    public static boolean isArray_payload_rst_cnt() {
        return false;
    }

    /**
     * Return the offset (in bytes) of the field 'payload.rst_cnt'
     */
    public static int offset_payload_rst_cnt() {
        return (56 / 8);
    }

    /**
     * Return the offset (in bits) of the field 'payload.rst_cnt'
     */
    public static int offsetBits_payload_rst_cnt() {
        return 56;
    }

    /**
     * Return the value (as a int) of the field 'payload.rst_cnt'
     */
    public int get_payload_rst_cnt() {
        return (int)getUIntBEElement(offsetBits_payload_rst_cnt(), 16);
    }

    /**
     * Set the value of the field 'payload.rst_cnt'
     */
    public void set_payload_rst_cnt(int value) {
        setUIntBEElement(offsetBits_payload_rst_cnt(), 16, value);
    }

    /**
     * Return the size, in bytes, of the field 'payload.rst_cnt'
     */
    public static int size_payload_rst_cnt() {
        return (16 / 8);
    }

    /**
     * Return the size, in bits, of the field 'payload.rst_cnt'
     */
    public static int sizeBits_payload_rst_cnt() {
        return 16;
    }

    /////////////////////////////////////////////////////////
    // Accessor methods for field: payload.rst_flag
    //   Field type: short
    //   Offset (bits): 72
    //   Size (bits): 8
    /////////////////////////////////////////////////////////

    /**
     * Return whether the field 'payload.rst_flag' is signed (false).
     */
    public static boolean isSigned_payload_rst_flag() {
        return false;
    }

    /**
     * Return whether the field 'payload.rst_flag' is an array (false).
     */
    public static boolean isArray_payload_rst_flag() {
        return false;
    }

    /**
     * Return the offset (in bytes) of the field 'payload.rst_flag'
     */
    public static int offset_payload_rst_flag() {
        return (72 / 8);
    }

    /**
     * Return the offset (in bits) of the field 'payload.rst_flag'
     */
    public static int offsetBits_payload_rst_flag() {
        return 72;
    }

    /**
     * Return the value (as a short) of the field 'payload.rst_flag'
     */
    public short get_payload_rst_flag() {
        return (short)getUIntBEElement(offsetBits_payload_rst_flag(), 8);
    }

    /**
     * Set the value of the field 'payload.rst_flag'
     */
    public void set_payload_rst_flag(short value) {
        setUIntBEElement(offsetBits_payload_rst_flag(), 8, value);
    }

    /**
     * Return the size, in bytes, of the field 'payload.rst_flag'
     */
    public static int size_payload_rst_flag() {
        return (8 / 8);
    }

    /**
     * Return the size, in bits, of the field 'payload.rst_flag'
     */
    public static int sizeBits_payload_rst_flag() {
        return 8;
    }

    /////////////////////////////////////////////////////////
    // Accessor methods for field: payload.compiler_desc1
    //   Field type: short
    //   Offset (bits): 80
    //   Size (bits): 8
    /////////////////////////////////////////////////////////

    /**
     * Return whether the field 'payload.compiler_desc1' is signed (false).
     */
    public static boolean isSigned_payload_compiler_desc1() {
        return false;
    }

    /**
     * Return whether the field 'payload.compiler_desc1' is an array (false).
     */
    public static boolean isArray_payload_compiler_desc1() {
        return false;
    }

    /**
     * Return the offset (in bytes) of the field 'payload.compiler_desc1'
     */
    public static int offset_payload_compiler_desc1() {
        return (80 / 8);
    }

    /**
     * Return the offset (in bits) of the field 'payload.compiler_desc1'
     */
    public static int offsetBits_payload_compiler_desc1() {
        return 80;
    }

    /**
     * Return the value (as a short) of the field 'payload.compiler_desc1'
     */
    public short get_payload_compiler_desc1() {
        return (short)getUIntBEElement(offsetBits_payload_compiler_desc1(), 8);
    }

    /**
     * Set the value of the field 'payload.compiler_desc1'
     */
    public void set_payload_compiler_desc1(short value) {
        setUIntBEElement(offsetBits_payload_compiler_desc1(), 8, value);
    }

    /**
     * Return the size, in bytes, of the field 'payload.compiler_desc1'
     */
    public static int size_payload_compiler_desc1() {
        return (8 / 8);
    }

    /**
     * Return the size, in bits, of the field 'payload.compiler_desc1'
     */
    public static int sizeBits_payload_compiler_desc1() {
        return 8;
    }

    /////////////////////////////////////////////////////////
    // Accessor methods for field: payload.compiler_desc2
    //   Field type: short
    //   Offset (bits): 88
    //   Size (bits): 8
    /////////////////////////////////////////////////////////

    /**
     * Return whether the field 'payload.compiler_desc2' is signed (false).
     */
    public static boolean isSigned_payload_compiler_desc2() {
        return false;
    }

    /**
     * Return whether the field 'payload.compiler_desc2' is an array (false).
     */
    public static boolean isArray_payload_compiler_desc2() {
        return false;
    }

    /**
     * Return the offset (in bytes) of the field 'payload.compiler_desc2'
     */
    public static int offset_payload_compiler_desc2() {
        return (88 / 8);
    }

    /**
     * Return the offset (in bits) of the field 'payload.compiler_desc2'
     */
    public static int offsetBits_payload_compiler_desc2() {
        return 88;
    }

    /**
     * Return the value (as a short) of the field 'payload.compiler_desc2'
     */
    public short get_payload_compiler_desc2() {
        return (short)getUIntBEElement(offsetBits_payload_compiler_desc2(), 8);
    }

    /**
     * Set the value of the field 'payload.compiler_desc2'
     */
    public void set_payload_compiler_desc2(short value) {
        setUIntBEElement(offsetBits_payload_compiler_desc2(), 8, value);
    }

    /**
     * Return the size, in bytes, of the field 'payload.compiler_desc2'
     */
    public static int size_payload_compiler_desc2() {
        return (8 / 8);
    }

    /**
     * Return the size, in bits, of the field 'payload.compiler_desc2'
     */
    public static int sizeBits_payload_compiler_desc2() {
        return 8;
    }

    /////////////////////////////////////////////////////////
    // Accessor methods for field: payload.compiler_desc3
    //   Field type: short
    //   Offset (bits): 96
    //   Size (bits): 8
    /////////////////////////////////////////////////////////

    /**
     * Return whether the field 'payload.compiler_desc3' is signed (false).
     */
    public static boolean isSigned_payload_compiler_desc3() {
        return false;
    }

    /**
     * Return whether the field 'payload.compiler_desc3' is an array (false).
     */
    public static boolean isArray_payload_compiler_desc3() {
        return false;
    }

    /**
     * Return the offset (in bytes) of the field 'payload.compiler_desc3'
     */
    public static int offset_payload_compiler_desc3() {
        return (96 / 8);
    }

    /**
     * Return the offset (in bits) of the field 'payload.compiler_desc3'
     */
    public static int offsetBits_payload_compiler_desc3() {
        return 96;
    }

    /**
     * Return the value (as a short) of the field 'payload.compiler_desc3'
     */
    public short get_payload_compiler_desc3() {
        return (short)getUIntBEElement(offsetBits_payload_compiler_desc3(), 8);
    }

    /**
     * Set the value of the field 'payload.compiler_desc3'
     */
    public void set_payload_compiler_desc3(short value) {
        setUIntBEElement(offsetBits_payload_compiler_desc3(), 8, value);
    }

    /**
     * Return the size, in bytes, of the field 'payload.compiler_desc3'
     */
    public static int size_payload_compiler_desc3() {
        return (8 / 8);
    }

    /**
     * Return the size, in bits, of the field 'payload.compiler_desc3'
     */
    public static int sizeBits_payload_compiler_desc3() {
        return 8;
    }

    /////////////////////////////////////////////////////////
    // Accessor methods for field: payload.compiler_desc4
    //   Field type: short
    //   Offset (bits): 104
    //   Size (bits): 8
    /////////////////////////////////////////////////////////

    /**
     * Return whether the field 'payload.compiler_desc4' is signed (false).
     */
    public static boolean isSigned_payload_compiler_desc4() {
        return false;
    }

    /**
     * Return whether the field 'payload.compiler_desc4' is an array (false).
     */
    public static boolean isArray_payload_compiler_desc4() {
        return false;
    }

    /**
     * Return the offset (in bytes) of the field 'payload.compiler_desc4'
     */
    public static int offset_payload_compiler_desc4() {
        return (104 / 8);
    }

    /**
     * Return the offset (in bits) of the field 'payload.compiler_desc4'
     */
    public static int offsetBits_payload_compiler_desc4() {
        return 104;
    }

    /**
     * Return the value (as a short) of the field 'payload.compiler_desc4'
     */
    public short get_payload_compiler_desc4() {
        return (short)getUIntBEElement(offsetBits_payload_compiler_desc4(), 8);
    }

    /**
     * Set the value of the field 'payload.compiler_desc4'
     */
    public void set_payload_compiler_desc4(short value) {
        setUIntBEElement(offsetBits_payload_compiler_desc4(), 8, value);
    }

    /**
     * Return the size, in bytes, of the field 'payload.compiler_desc4'
     */
    public static int size_payload_compiler_desc4() {
        return (8 / 8);
    }

    /**
     * Return the size, in bits, of the field 'payload.compiler_desc4'
     */
    public static int sizeBits_payload_compiler_desc4() {
        return 8;
    }

    /////////////////////////////////////////////////////////
    // Accessor methods for field: payload.compiler_ver
    //   Field type: long
    //   Offset (bits): 112
    //   Size (bits): 32
    /////////////////////////////////////////////////////////

    /**
     * Return whether the field 'payload.compiler_ver' is signed (false).
     */
    public static boolean isSigned_payload_compiler_ver() {
        return false;
    }

    /**
     * Return whether the field 'payload.compiler_ver' is an array (false).
     */
    public static boolean isArray_payload_compiler_ver() {
        return false;
    }

    /**
     * Return the offset (in bytes) of the field 'payload.compiler_ver'
     */
    public static int offset_payload_compiler_ver() {
        return (112 / 8);
    }

    /**
     * Return the offset (in bits) of the field 'payload.compiler_ver'
     */
    public static int offsetBits_payload_compiler_ver() {
        return 112;
    }

    /**
     * Return the value (as a long) of the field 'payload.compiler_ver'
     */
    public long get_payload_compiler_ver() {
        return (long)getUIntBEElement(offsetBits_payload_compiler_ver(), 32);
    }

    /**
     * Set the value of the field 'payload.compiler_ver'
     */
    public void set_payload_compiler_ver(long value) {
        setUIntBEElement(offsetBits_payload_compiler_ver(), 32, value);
    }

    /**
     * Return the size, in bytes, of the field 'payload.compiler_ver'
     */
    public static int size_payload_compiler_ver() {
        return (32 / 8);
    }

    /**
     * Return the size, in bits, of the field 'payload.compiler_ver'
     */
    public static int sizeBits_payload_compiler_ver() {
        return 32;
    }

    /////////////////////////////////////////////////////////
    // Accessor methods for field: payload.compile_date
    //   Field type: long
    //   Offset (bits): 144
    //   Size (bits): 32
    /////////////////////////////////////////////////////////

    /**
     * Return whether the field 'payload.compile_date' is signed (false).
     */
    public static boolean isSigned_payload_compile_date() {
        return false;
    }

    /**
     * Return whether the field 'payload.compile_date' is an array (false).
     */
    public static boolean isArray_payload_compile_date() {
        return false;
    }

    /**
     * Return the offset (in bytes) of the field 'payload.compile_date'
     */
    public static int offset_payload_compile_date() {
        return (144 / 8);
    }

    /**
     * Return the offset (in bits) of the field 'payload.compile_date'
     */
    public static int offsetBits_payload_compile_date() {
        return 144;
    }

    /**
     * Return the value (as a long) of the field 'payload.compile_date'
     */
    public long get_payload_compile_date() {
        return (long)getUIntBEElement(offsetBits_payload_compile_date(), 32);
    }

    /**
     * Set the value of the field 'payload.compile_date'
     */
    public void set_payload_compile_date(long value) {
        setUIntBEElement(offsetBits_payload_compile_date(), 32, value);
    }

    /**
     * Return the size, in bytes, of the field 'payload.compile_date'
     */
    public static int size_payload_compile_date() {
        return (32 / 8);
    }

    /**
     * Return the size, in bits, of the field 'payload.compile_date'
     */
    public static int sizeBits_payload_compile_date() {
        return 32;
    }

    /////////////////////////////////////////////////////////
    // Accessor methods for field: payload.fw_ver
    //   Field type: int
    //   Offset (bits): 176
    //   Size (bits): 16
    /////////////////////////////////////////////////////////

    /**
     * Return whether the field 'payload.fw_ver' is signed (false).
     */
    public static boolean isSigned_payload_fw_ver() {
        return false;
    }

    /**
     * Return whether the field 'payload.fw_ver' is an array (false).
     */
    public static boolean isArray_payload_fw_ver() {
        return false;
    }

    /**
     * Return the offset (in bytes) of the field 'payload.fw_ver'
     */
    public static int offset_payload_fw_ver() {
        return (176 / 8);
    }

    /**
     * Return the offset (in bits) of the field 'payload.fw_ver'
     */
    public static int offsetBits_payload_fw_ver() {
        return 176;
    }

    /**
     * Return the value (as a int) of the field 'payload.fw_ver'
     */
    public int get_payload_fw_ver() {
        return (int)getUIntBEElement(offsetBits_payload_fw_ver(), 16);
    }

    /**
     * Set the value of the field 'payload.fw_ver'
     */
    public void set_payload_fw_ver(int value) {
        setUIntBEElement(offsetBits_payload_fw_ver(), 16, value);
    }

    /**
     * Return the size, in bytes, of the field 'payload.fw_ver'
     */
    public static int size_payload_fw_ver() {
        return (16 / 8);
    }

    /**
     * Return the size, in bits, of the field 'payload.fw_ver'
     */
    public static int sizeBits_payload_fw_ver() {
        return 16;
    }

    /////////////////////////////////////////////////////////
    // Accessor methods for field: payload.sw_rev_id
    //   Field type: long
    //   Offset (bits): 192
    //   Size (bits): 32
    /////////////////////////////////////////////////////////

    /**
     * Return whether the field 'payload.sw_rev_id' is signed (false).
     */
    public static boolean isSigned_payload_sw_rev_id() {
        return false;
    }

    /**
     * Return whether the field 'payload.sw_rev_id' is an array (false).
     */
    public static boolean isArray_payload_sw_rev_id() {
        return false;
    }

    /**
     * Return the offset (in bytes) of the field 'payload.sw_rev_id'
     */
    public static int offset_payload_sw_rev_id() {
        return (192 / 8);
    }

    /**
     * Return the offset (in bits) of the field 'payload.sw_rev_id'
     */
    public static int offsetBits_payload_sw_rev_id() {
        return 192;
    }

    /**
     * Return the value (as a long) of the field 'payload.sw_rev_id'
     */
    public long get_payload_sw_rev_id() {
        return (long)getUIntBEElement(offsetBits_payload_sw_rev_id(), 32);
    }

    /**
     * Set the value of the field 'payload.sw_rev_id'
     */
    public void set_payload_sw_rev_id(long value) {
        setUIntBEElement(offsetBits_payload_sw_rev_id(), 32, value);
    }

    /**
     * Return the size, in bytes, of the field 'payload.sw_rev_id'
     */
    public static int size_payload_sw_rev_id() {
        return (32 / 8);
    }

    /**
     * Return the size, in bits, of the field 'payload.sw_rev_id'
     */
    public static int sizeBits_payload_sw_rev_id() {
        return 32;
    }

}
