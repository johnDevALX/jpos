package net.ekene.jposclient.iso;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.ekene.jposclient.iso.utils.PinBlockUtilities;
import org.jpos.iso.ISODate;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.TimeZone;

@RequiredArgsConstructor
@Slf4j
@Service
public class MessageBuilder {
    private final PinBlockUtilities pinBlockUtilities;
    Date date = new Date();
    TimeZone timeZone = TimeZone.getTimeZone("GMT+1");

    public ISOMsg buildEchoMsg() {
        try {


            ISOMsg isoMsg = new ISOMsg();
            isoMsg.setMTI("0800");
            isoMsg.set(3, "9D0000");
            isoMsg.set(7, ISODate.getDateTime(date, timeZone));
            isoMsg.set(11, "000001");
            isoMsg.set(12, ISODate.getTime(date, timeZone));
            isoMsg.set(13, ISODate.getDate(date, timeZone));
            isoMsg.set(70, "301");

            return isoMsg;
        } catch (ISOException e) {
            throw new RuntimeException(e);
        }
    }

    public ISOMsg buildKeyExchangeMsg() {
        try {

            ISOMsg isoMsg = new ISOMsg();
            isoMsg.setMTI("0800");
            isoMsg.set(3, "920000");
            isoMsg.set(7, ISODate.getDateTime(date, timeZone));
            isoMsg.set(11, "000001");
            isoMsg.set(12, ISODate.getTime(date, timeZone));
            isoMsg.set(13, ISODate.getDate(date, timeZone));
            isoMsg.set(32, "040");
            isoMsg.set(70, "101");
            isoMsg.set(100, "058");

            return isoMsg;
        } catch (ISOException e) {
            throw new RuntimeException(e);
        }
    }

    public ISOMsg buildTransferMsg() {
        try {
            String pan = "5559405048128222";
            byte[] bytes = pinBlockUtilities.generatePinBlock("1234", "C7153E524C07643D", pan);

            String amount = pinBlockUtilities.formulateDE4(1000);
            ISOMsg isoMsg = new ISOMsg();
            isoMsg.setMTI("0200");
            isoMsg.set(2, pan);
            isoMsg.set(3, "920000");
            isoMsg.set(4, amount);
            isoMsg.set(7, "0905091101");
            isoMsg.set(11, "642795");
            isoMsg.set(32, "4008");
            isoMsg.set(37, "451298");
            isoMsg.set(41, "20351254");
            isoMsg.set(49, "566");
            isoMsg.set(52, bytes);
            isoMsg.set(70, "002");
            isoMsg.set(100, "082");

            return isoMsg;
        } catch (ISOException e) {
            throw new RuntimeException(e);
        }
    }

}
