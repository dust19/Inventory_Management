package com.inventra.util;

import com.inventra.dto.response.PageResponse;
import org.springframework.data.domain.Page;

public class PageResponseUtil {

     public static <T> PageResponse<T> fromPage(Page<T> pageData) {

          PageResponse<T> response = new PageResponse<>();

          response.setContent(pageData.getContent());

          response.setPage(pageData.getNumber());
          response.setSize(pageData.getSize());

          response.setTotalElements(pageData.getTotalElements());
          response.setTotalPages(pageData.getTotalPages());

          response.setFirst(pageData.isFirst());
          response.setLast(pageData.isLast());

          return response;
     }
}