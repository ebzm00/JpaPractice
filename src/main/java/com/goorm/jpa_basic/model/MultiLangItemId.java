package com.goorm.jpa_basic.model;

import jakarta.persistence.Embeddable;
import jakarta.persistence.ManyToOne;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class MultiLangItemId implements Serializable {

    @ManyToOne
    private Lang lang_id;

    @ManyToOne
    private Item item_id;

    // Getters, Setters, hashCode, and equals methods

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MultiLangItemId that = (MultiLangItemId) o;
        return Objects.equals(lang_id, that.lang_id) && Objects.equals(item_id, that.item_id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lang_id, item_id);
    }
}