package com.example.matdongsanserver.domain.dashboard.exception;

import com.example.matdongsanserver.common.exception.BusinessException;
import lombok.Getter;

@Getter
public class DashboardException extends BusinessException {
    private final DashboardErrorCode dashboardErrorCode;

    public DashboardException(DashboardErrorCode dashboardErrorCode) {
        super(dashboardErrorCode);
        this.dashboardErrorCode = dashboardErrorCode;
    }
}
