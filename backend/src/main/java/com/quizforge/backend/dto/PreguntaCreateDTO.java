package com.quizforge.backend.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "tipo"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = PreguntaMultipleChoiceDTO.class, name = "MULTIPLE_CHOICE"),
    @JsonSubTypes.Type(value = PreguntaVerdaderoFalsoDTO.class, name = "VERDADERO_FALSO"),
    @JsonSubTypes.Type(value = PreguntaDesarrolloDeterministicoDTO.class, name = "DESARROLLO_DETERMINISTICO"),
    @JsonSubTypes.Type(value = PreguntaDesarrolloNoDeterministicoDTO.class, name = "DESARROLLO_NO_DETERMINISTICO")
})
public sealed interface PreguntaCreateDTO permits
        PreguntaMultipleChoiceDTO,
        PreguntaVerdaderoFalsoDTO,
        PreguntaDesarrolloDeterministicoDTO,
        PreguntaDesarrolloNoDeterministicoDTO {

    String texto();
    Integer examenId();
}
