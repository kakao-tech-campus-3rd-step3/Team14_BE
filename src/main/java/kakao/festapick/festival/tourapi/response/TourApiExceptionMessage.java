package kakao.festapick.festival.tourapi.response;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;

@Getter
@JacksonXmlRootElement(localName = "OpenAPI_ServiceResponse")
public class TourApiExceptionMessage {

    private CmmMsgHeader cmmMsgHeader;

    @Getter
    public static class CmmMsgHeader {

        private String errMsg;

        private String returnAuthMsg;

        private int returnReasonCode;
    }
}
