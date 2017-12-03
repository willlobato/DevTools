package devtools.liberty.serverxml;

import devtools.liberty.serverxml.annotation.Attribute;
import devtools.liberty.serverxml.annotation.TagName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TagName("enterpriseApplication")
public class EnterpriseApplication {

    @Attribute("id")
    private String id;

    @Attribute("location")
    private String location;

    @Attribute("name")
    private String name;

}
