package studio.wormhole.almaserver.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import studio.wormhole.almaserver.enums.State;


import java.util.Date;

@Data
@SuperBuilder(toBuilder = true)


public class Project  extends TokenBase{

    private long id;
    private String name;
    private String context;
    private String icon;
    private State state;
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date createAt;


}
