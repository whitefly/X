package com.entity;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Document("parser")
public class AjaxParserDO extends IndexParserDO {

}
