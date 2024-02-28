/**
 * This class is automatically generated by mig. DO NOT EDIT THIS FILE.
 * This class implements a Java interface to the 'DozerNodeInfoMsg'
 * message type.
 */

 package ch.epfl.gsn.wrappers.backlog.plugins.tinyos2x;

public class DozerNodeInfoMsg extends DataHeaderMsg {

    /** The default size of this message type in bytes. */
    public static final int DEFAULT_MESSAGE_SIZE = 20;

    /** The Active Message type associated with this message. */
    public static final int AM_TYPE = 130;

    /** Create a new DozerNodeInfoMsg of size 20. */
    public DozerNodeInfoMsg() {
        super(DEFAULT_MESSAGE_SIZE);
        amTypeSet(AM_TYPE);
    }

    /** Create a new DozerNodeInfoMsg of the given data_length. */
    public DozerNodeInfoMsg(int data_length) {
        super(data_length);
        amTypeSet(AM_TYPE);
    }

    /**
     * Create a new DozerNodeInfoMsg with the given data_length
     * and base offset.
     */
    public DozerNodeInfoMsg(int data_length, int base_offset) {
        super(data_length, base_offset);
        amTypeSet(AM_TYPE);
    }

    /**
     * Create a new DozerNodeInfoMsg using the given byte array
     * as backing store.
     */
    public DozerNodeInfoMsg(byte[] data) {
        super(data);
        amTypeSet(AM_TYPE);
    }

    /**
     * Create a new DozerNodeInfoMsg using the given byte array
     * as backing store, with the given base offset.
     */
    public DozerNodeInfoMsg(byte[] data, int base_offset) {
        super(data, base_offset);
        amTypeSet(AM_TYPE);
    }

    /**
     * Create a new DozerNodeInfoMsg using the given byte array
     * as backing store, with the given base offset and data length.
     */
    public DozerNodeInfoMsg(byte[] data, int base_offset, int data_length) {
        super(data, base_offset, data_length);
        amTypeSet(AM_TYPE);
    }

    /**
     * Create a new DozerNodeInfoMsg embedded in the given message
     * at the given base offset.
     */
    public DozerNodeInfoMsg(net.tinyos.message.Message msg, int base_offset) {
        super(msg, base_offset, DEFAULT_MESSAGE_SIZE);
        amTypeSet(AM_TYPE);
    }

    /**
     * Create a new DozerNodeInfoMsg embedded in the given message
     * at the given base offset and length.
     */
    public DozerNodeInfoMsg(net.tinyos.message.Message msg, int base_offset, int data_length) {
        super(msg, base_offset, data_length);
        amTypeSet(AM_TYPE);
    }

    /**
    /* Return a String representation of this message. Includes the
     * message type name and the non-indexed field values.
     */
    public String toString() {
      String s = "Message <DozerNodeInfoMsg> \n";
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
        s += "  [payload.Id1=0x"+Long.toHexString(get_payload_Id1())+"]\n";
      } catch (ArrayIndexOutOfBoundsException aioobe) { /* Skip field */ }
      try {
        s += "  [payload.Id2=0x"+Long.toHexString(get_payload_Id2())+"]\n";
      } catch (ArrayIndexOutOfBoundsException aioobe) { /* Skip field */ }
      try {
        s += "  [payload.RadioChannel=0x"+Long.toHexString(get_payload_RadioChannel())+"]\n";
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
    // Accessor methods for field: payload.Id1
    //   Field type: long
    //   Offset (bits): 56
    //   Size (bits): 48
    /////////////////////////////////////////////////////////

    /**
     * Return whether the field 'payload.Id1' is signed (false).
     */
    public static boolean isSigned_payload_Id1() {
        return false;
    }

    /**
     * Return whether the field 'payload.Id1' is an array (false).
     */
    public static boolean isArray_payload_Id1() {
        return false;
    }

    /**
     * Return the offset (in bytes) of the field 'payload.Id1'
     */
    public static int offset_payload_Id1() {
        return (56 / 8);
    }

    /**
     * Return the offset (in bits) of the field 'payload.Id1'
     */
    public static int offsetBits_payload_Id1() {
        return 56;
    }

    /**
     * Return the value (as a long) of the field 'payload.Id1'
     */
    public long get_payload_Id1() {
        return (long)getUIntBEElement(offsetBits_payload_Id1(), 48);
    }

    /**
     * Set the value of the field 'payload.Id1'
     */
    public void set_payload_Id1(long value) {
        setUIntBEElement(offsetBits_payload_Id1(), 48, value);
    }

    /**
     * Return the size, in bytes, of the field 'payload.Id1'
     */
    public static int size_payload_Id1() {
        return (48 / 8);
    }

    /**
     * Return the size, in bits, of the field 'payload.Id1'
     */
    public static int sizeBits_payload_Id1() {
        return 48;
    }

    /////////////////////////////////////////////////////////
    // Accessor methods for field: payload.Id2
    //   Field type: long
    //   Offset (bits): 104
    //   Size (bits): 48
    /////////////////////////////////////////////////////////

    /**
     * Return whether the field 'payload.Id2' is signed (false).
     */
    public static boolean isSigned_payload_Id2() {
        return false;
    }

    /**
     * Return whether the field 'payload.Id2' is an array (false).
     */
    public static boolean isArray_payload_Id2() {
        return false;
    }

    /**
     * Return the offset (in bytes) of the field 'payload.Id2'
     */
    public static int offset_payload_Id2() {
        return (104 / 8);
    }

    /**
     * Return the offset (in bits) of the field 'payload.Id2'
     */
    public static int offsetBits_payload_Id2() {
        return 104;
    }

    /**
     * Return the value (as a long) of the field 'payload.Id2'
     */
    public long get_payload_Id2() {
        return (long)getUIntBEElement(offsetBits_payload_Id2(), 48);
    }

    /**
     * Set the value of the field 'payload.Id2'
     */
    public void set_payload_Id2(long value) {
        setUIntBEElement(offsetBits_payload_Id2(), 48, value);
    }

    /**
     * Return the size, in bytes, of the field 'payload.Id2'
     */
    public static int size_payload_Id2() {
        return (48 / 8);
    }

    /**
     * Return the size, in bits, of the field 'payload.Id2'
     */
    public static int sizeBits_payload_Id2() {
        return 48;
    }

    /////////////////////////////////////////////////////////
    // Accessor methods for field: payload.RadioChannel
    //   Field type: short
    //   Offset (bits): 152
    //   Size (bits): 8
    /////////////////////////////////////////////////////////

    /**
     * Return whether the field 'payload.RadioChannel' is signed (false).
     */
    public static boolean isSigned_payload_RadioChannel() {
        return false;
    }

    /**
     * Return whether the field 'payload.RadioChannel' is an array (false).
     */
    public static boolean isArray_payload_RadioChannel() {
        return false;
    }

    /**
     * Return the offset (in bytes) of the field 'payload.RadioChannel'
     */
    public static int offset_payload_RadioChannel() {
        return (152 / 8);
    }

    /**
     * Return the offset (in bits) of the field 'payload.RadioChannel'
     */
    public static int offsetBits_payload_RadioChannel() {
        return 152;
    }

    /**
     * Return the value (as a short) of the field 'payload.RadioChannel'
     */
    public short get_payload_RadioChannel() {
        return (short)getUIntBEElement(offsetBits_payload_RadioChannel(), 8);
    }

    /**
     * Set the value of the field 'payload.RadioChannel'
     */
    public void set_payload_RadioChannel(short value) {
        setUIntBEElement(offsetBits_payload_RadioChannel(), 8, value);
    }

    /**
     * Return the size, in bytes, of the field 'payload.RadioChannel'
     */
    public static int size_payload_RadioChannel() {
        return (8 / 8);
    }

    /**
     * Return the size, in bits, of the field 'payload.RadioChannel'
     */
    public static int sizeBits_payload_RadioChannel() {
        return 8;
    }

}
