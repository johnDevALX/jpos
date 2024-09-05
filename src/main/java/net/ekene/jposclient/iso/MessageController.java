package net.ekene.jposclient.iso;

import lombok.extern.slf4j.Slf4j;
import net.ekene.jposclient.iso.utils.PinBlockUtilities;
import org.jpos.iso.ISOChannel;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.channel.PostChannel;
import org.jpos.iso.packager.GenericPackager;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

@RestController
@RequestMapping("/v1/api")
@Slf4j
public class MessageController {
    private final String HOST = "52.234.156.59";
//    private final int PORT = 29001;
    private final int PORT = 12000;
    private GenericPackager genericPackager;
    private ISOChannel channel;

    //    @Autowired
    private final  MessageBuilder isoService;
    private final PinBlockUtilities pinBlockUtilities;


    public MessageController(MessageBuilder muxController, PinBlockUtilities pinBlockUtilities) throws ISOException, IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("jpos.xml");
        this.genericPackager =  new GenericPackager(is);

        this.channel = new PostChannel(HOST, PORT, this.genericPackager);
        this.channel.connect();
        this.isoService = muxController;
        this.pinBlockUtilities = pinBlockUtilities;
    }

    @PostMapping("/echo")
    public void sendEchoMsg() {
        try {
            ISOMsg echoRequest = isoService.buildEchoMsg();

            if(echoRequest != null) {
                echoRequest.setPackager(this.genericPackager);

                channel.send(echoRequest);

                ISOMsg echoResponse = channel.receive();
                String mti = echoResponse.getMTI();
                String res = echoResponse.getString(39);

                log.info("mti {}", mti);
                log.info("res {}", res);
            }
        } catch (ISOException | IOException e) {
            throw new RuntimeException(e);
        }

    }

    @PostMapping("/key-exchange")
    public void callHomeForKeyExchange(){
        try {
            ISOMsg keyExchangeMsg = isoService.buildKeyExchangeMsg();

            if (Objects.nonNull(keyExchangeMsg)){
                keyExchangeMsg.setPackager(this.genericPackager);

                channel.send(keyExchangeMsg);
                ISOMsg keyExchangeResponse = channel.receive();
                String mti = keyExchangeResponse.getMTI();
                String zpk = keyExchangeResponse.getString(53);
                String res = keyExchangeResponse.getString(39);

                pinBlockUtilities.addEZpk("zpk", zpk);
                log.info("mti {}", mti);
                log.info("zpk {}", zpk);
                log.info("res {}", res);
            }

        } catch (ISOException | IOException e) {
            throw new RuntimeException(e);
        }

    }

    @PostMapping("/fin-message")
    public void makeFinancialCall(){
        try {
            ISOMsg transferMsg = isoService.buildTransferMsg();

            if (Objects.nonNull(transferMsg)){
                transferMsg.setPackager(this.genericPackager);

                channel.send(transferMsg);
                ISOMsg transferMsgResponse = channel.receive();
                String mti = transferMsgResponse.getMTI();
                String res = transferMsgResponse.getString(39);

                log.info("mti {}", mti);
                log.info("res {}", res);
            }

        } catch (ISOException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
