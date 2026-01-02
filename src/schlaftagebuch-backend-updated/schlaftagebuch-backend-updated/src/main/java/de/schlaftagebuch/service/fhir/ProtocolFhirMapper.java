package de.schlaftagebuch.service.fhir;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.schlaftagebuch.model.ProtocolSubmissionEntity;
import org.hl7.fhir.r4.model.QuestionnaireResponse;
import org.hl7.fhir.r4.model.Reference;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ProtocolFhirMapper {

    private final ObjectMapper om = new ObjectMapper();

    public QuestionnaireResponse toQuestionnaireResponse(ProtocolSubmissionEntity e) throws Exception {
        QuestionnaireResponse qr = new QuestionnaireResponse();
        qr.setStatus(QuestionnaireResponse.QuestionnaireResponseStatus.COMPLETED);
        qr.setSubject(new Reference(e.getSubjectRef()));
        qr.setAuthored(java.util.Date.from(e.getFilledAt()));
        qr.setQuestionnaire("Questionnaire/" + e.getTemplateKey());

        // answersJson = [{id,result},...]
        List<Map<String, Object>> answers =
                om.readValue(e.getAnswersJson(), new TypeReference<>() {});

        for (Map<String, Object> a : answers) {
            String id = String.valueOf(a.get("id"));
            String result = String.valueOf(a.get("result"));

            QuestionnaireResponse.QuestionnaireResponseItemComponent item =
                    new QuestionnaireResponse.QuestionnaireResponseItemComponent();
            item.setLinkId(id);
            item.addAnswer().setValue(new org.hl7.fhir.r4.model.StringType(result));
            qr.addItem(item);
        }

        return qr;
    }
}
