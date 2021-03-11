package com.entity;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document("field")
public class NewsField extends FieldDO {
}
