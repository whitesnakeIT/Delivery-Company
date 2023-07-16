package com.kapusniak.tomasz.handler;

import com.kapusniak.tomasz.openapi.model.ApiError;
import com.kapusniak.tomasz.openapi.model.ApiSubError;
import jakarta.persistence.EntityNotFoundException;
import org.hibernate.StaleObjectStateException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Service;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ApiErrorService {
    public ApiError createApiError(MethodArgumentNotValidException ex) {
        ApiError apiError = buildApiError(ex);
        apiError.setHttpStatus(ex.getStatusCode().value());
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        List<ApiSubError> subErrors = convertFieldErrorsToApiSubErrors(fieldErrors);
        apiError.setSubErrors(subErrors);

        return apiError;
    }

    private <T extends Exception> ApiError buildApiError(T ex) {
        ApiError apiError = new ApiError();
        apiError.setMessage(ex.getLocalizedMessage());
        apiError.setTimestamp(OffsetDateTime.now());

        return apiError;
    }

    private List<ApiSubError> convertFieldErrorsToApiSubErrors(List<FieldError> fieldErrors) {
        if (fieldErrors == null) {
            return new ArrayList<>();
        }
        return fieldErrors.stream()
                .map(fieldError -> {
                    ApiSubError apiSubError = new ApiSubError();
                    apiSubError.setField(fieldError.getField());
                    apiSubError.setRejectedValue(String.valueOf(fieldError.getRejectedValue()));
                    apiSubError.setMessage(String.valueOf(fieldError.getDefaultMessage()));
                    return apiSubError;
                })
                .toList();

    }

    public ApiError createApiError(EntityNotFoundException ex) {
        ApiError apiError = buildApiError(ex);
        apiError.setHttpStatus(HttpStatus.NOT_FOUND.value());

        return apiError;
    }

    public ApiError createApiError(HttpMessageNotReadableException ex) {
        ApiError apiError = buildApiError(ex);

        return apiError;
    }

    public ApiError createApiError(StaleObjectStateException ex) {
        ApiError apiError = buildApiError(ex);

        return apiError;

    }
}
