package com.soecode.lyf.entity;

import lombok.*;

import java.util.Date;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Persion {
    private Long id;
    private String name;
    private Integer age;
    private Date birthday;
}
