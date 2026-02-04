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
import com.poly.quanlykhohang.dto.dashboard.TransferChartDTO;

@Service
public class DashboardService {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    public DashboardResponse getDashboardData(Integer maKho) { // Xóa tham số 'nam' ở đây
        DashboardResponse response = new DashboardResponse();

        try {
            // 1. Khởi tạo Stored Procedure Query
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_GetDashboardStats");

            // 2. Đăng ký tham số (CHỈ ĐĂNG KÝ 1 THAM SỐ MaKho)
            query.registerStoredProcedureParameter("MaKho", Integer.class, ParameterMode.IN);
            // [ĐÃ XÓA] query.registerStoredProcedureParameter("Nam", ...); <-- Xóa dòng này

            // 3. Truyền giá trị
            query.setParameter("MaKho", maKho != null ? maKho : 0);
            // [ĐÃ XÓA] query.setParameter("Nam", nam); <-- Xóa dòng này

            // 4. Thực thi và lấy ResultSet 1 (Cards)
            query.execute();

            List<Object[]> result1 = query.getResultList();
            if (!result1.isEmpty()) {
                Object[] row = result1.get(0);
                DashboardCardDTO card = new DashboardCardDTO(
                        row[0] != null ? (Integer) row[0] : 0, // TotalStock
                        row[1] != null ? (Integer) row[1] : 0, // ImportMonth
                        row[2] != null ? (Integer) row[2] : 0, // ExportMonth
                        null // ChartYear (SQL không trả về cột này nữa, để null hoặc set cứng 2026)
                );
                response.setCards(card);
            }

            // 5. Chuyển sang ResultSet 2 (Chart)
            if (query.hasMoreResults()) {
                List<Object[]> result2 = query.getResultList();
                List<DashboardChartDTO> chartList = new ArrayList<>();

                for (Object[] row : result2) {
                    chartList.add(new DashboardChartDTO(
                            (Integer) row[0], // Month
                            (Integer) row[1], // ImportQty
                            (Integer) row[2]  // ExportQty
                    ));
                }
                response.setChart(chartList);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return response;
    }

    @Transactional(readOnly = true)
    public List<TransferChartDTO> getTransferChartData(Integer maKho, Integer nam) {
        List<TransferChartDTO> chartList = new ArrayList<>();

        try {
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_ThongKe_ChuyenKho_TheoThang");

            // Đăng ký tham số
            query.registerStoredProcedureParameter("MaKho", Integer.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("Nam", Integer.class, ParameterMode.IN);

            // Truyền giá trị (Mặc định năm nay nếu null)
            int currentYear = (nam != null) ? nam : java.time.Year.now().getValue();

            query.setParameter("MaKho", maKho != null ? maKho : 0);
            query.setParameter("Nam", currentYear);

            query.execute();

            List<Object[]> result = query.getResultList();
            for (Object[] row : result) {
                chartList.add(new TransferChartDTO(
                        (Integer) row[0], // Month
                        (Integer) row[1], // TransferInQty
                        (Integer) row[2]  // TransferOutQty
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return chartList;
    }
}