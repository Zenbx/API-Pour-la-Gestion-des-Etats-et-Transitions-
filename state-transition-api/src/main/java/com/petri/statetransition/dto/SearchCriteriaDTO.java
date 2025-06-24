package com.petri.statetransition.dto;


import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SearchCriteriaDTO(
        String name,
        List<String> states,
        String type,
        String priority,
        LocalDateTime createdAfter,
        LocalDateTime createdBefore,
        Integer page,
        Integer size,
        String sortBy,
        String sortDirection
) {

    /**
     * Constructeur par défaut avec pagination
     */
    public static SearchCriteriaDTO withPagination(Integer page, Integer size) {
        return new SearchCriteriaDTO(null, null, null, null, null, null,
                page != null ? page : 0,
                size != null ? size : 20,
                "createdAt", "desc");
    }
}