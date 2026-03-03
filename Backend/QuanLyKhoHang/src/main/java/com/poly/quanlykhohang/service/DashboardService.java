package com.poly.quanlykhohang.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.StoredProcedureQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import com.poly.quanlykhohang.dto.dashboard.DashboardCardDTO;
import com.poly.quanlykhohang.dto.dashboard.DashboardChartDTO;
import com.poly.quanlykhohang.dto.dashboard.DashboardResponse;

@Service
public class DashboardService {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    public DashboardResponse getDashboardData(Integer maKho) {
        DashboardResponse response = new DashboardResponse();

        try {
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_GetDashboardStats");
            query.registerStoredProcedureParameter("MaKho", Integer.class, ParameterMode.IN);
            query.setParameter("MaKho", maKho != null ? maKho : 0);

            // THỰC THI (execute trả về true nếu kết quả đầu tiên là ResultSet)
            boolean hasResults = query.execute();

            if (hasResults) {
                // ĐỌC RESULT SET 1: Dữ liệu Cards (1 dòng)
                List<Object[]> result1 = query.getResultList();
                if (!result1.isEmpty()) {
                    Object[] row = result1.get(0);
                    DashboardCardDTO card = new DashboardCardDTO(
                            row[0] != null ? ((Number) row[0]).intValue() : 0, // Dùng Number cho an toàn ép kiểu
                            row[1] != null ? ((Number) row[1]).intValue() : 0,
                            row[2] != null ? ((Number) row[2]).intValue() : 0,
                            null
                    );
                    response.setCards(card);
                }

                // DI CHUYỂN ĐẾN RESULT SET 2 (Cực kỳ quan trọng)
                hasResults = query.hasMoreResults();

                if (hasResults) {
                    // ĐỌC RESULT SET 2: Dữ liệu Chart (12 dòng)
                    List<Object[]> result2 = query.getResultList();
                    List<DashboardChartDTO> chartList = new ArrayList<>();

                    for (Object[] row : result2) {
                        chartList.add(new DashboardChartDTO(
                                row[0] != null ? ((Number) row[0]).intValue() : 0,
                                row[1] != null ? ((Number) row[1]).intValue() : 0,
                                row[2] != null ? ((Number) row[2]).intValue() : 0
                        ));
                    }
                    response.setChart(chartList);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return response;
    }
}