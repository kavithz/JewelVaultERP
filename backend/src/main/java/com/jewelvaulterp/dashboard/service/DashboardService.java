package com.jewelvaulterp.dashboard.service;

import com.jewelvaulterp.company.repository.CompanyRepository;
import com.jewelvaulterp.dashboard.dto.DashboardSummaryResponse;
import com.jewelvaulterp.user.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    public DashboardService(
            CompanyRepository companyRepository,
            UserRepository userRepository
    ) {
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
    }

    public DashboardSummaryResponse getSummary() {
        long totalCompanies = companyRepository.count();
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByActiveTrue();
        long inactiveUsers = userRepository.countByActiveFalse();

        return new DashboardSummaryResponse(
                totalCompanies,
                totalUsers,
                activeUsers,
                inactiveUsers
        );
    }
}