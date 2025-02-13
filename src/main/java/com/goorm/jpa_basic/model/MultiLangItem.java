package com.goorm.jpa_basic.model;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MultiLangItem {

    @EmbeddedId
    private MultiLangItemId id;

    private String m_item_name;
    private String m_item_remark;
}