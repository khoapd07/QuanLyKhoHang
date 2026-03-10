<template>
  <div class="content-wrapper">
    <div class="content-header">
      <div class="container-fluid">
        <div class="row mb-2">
          <div class="col-sm-6 d-flex align-items-center">
            <h1 class="m-0">Báo Cáo Xuất Nhập Tồn</h1>
            <span v-if="isChotSoState === false || isChotSoState === 'false'" class="text-danger ml-2"
              style="cursor: pointer; font-size: 2rem;" title="Cảnh báo: Chưa chốt sổ đầu kỳ!"
              @click="showFirstYearWarning">
              <i class="fas fa-exclamation-circle">!</i>
            </span>
          </div>
        </div>
      </div>
    </div>

    <section class="content">
      <div class="container-fluid">

        <div class="card card-primary card-outline">
          <div class="card-header">
            <h3 class="card-title"><i class="fas fa-filter"></i> Bộ lọc báo cáo</h3>
          </div>
          <div class="card-body">
            <div class="row">
              <div class="col-lg-3 col-md-6 col-12">
                <div class="form-group">
                  <label>Từ ngày</label>
                  <input type="date" class="form-control" v-model="filters.startDate">
                </div>
              </div>
              <div class="col-lg-3 col-md-6 col-12">
                <div class="form-group">
                  <label>Đến ngày</label>
                  <input type="date" class="form-control" v-model="filters.endDate">
                </div>
              </div>
              <div class="col-lg-3 col-md-6 col-6">
                <div class="form-group">
                  <label>Kho/Chi nhánh</label>
                  <select class="form-control" v-model="filters.warehouseId" :disabled="!isAdmin">
                    <option :value="0" v-if="isAdmin">Tất cả kho</option>
                    <option v-for="k in khoList" :key="k.maKho" :value="k.maKho">{{ k.tenKho }}</option>
                  </select>
                </div>
              </div>

              <div class="col-lg-3 col-md-6 col-6">
                <div class="form-group">
                  <label>Trạng thái máy</label>
                  <select class="form-control" v-model="filters.statusId">
                    <!-- <option :value="-1">Tất cả</option> -->
                    <option v-for="st in statusList" :key="st.id" :value="st.id">{{ st.name }}</option>
                  </select>
                </div>
              </div>
            </div>
          </div>
          <div class="card-footer">
            <button type="button" class="btn btn-primary" @click="handleSearch" :disabled="loading">
              <i class="fas" :class="loading ? 'fa-spinner fa-spin' : 'fa-search'"></i>
              {{ loading ? 'Đang tải...' : 'Xem Báo Cáo' }}
            </button>

            <button v-if="allReportData.length > 0" type="button" class="btn btn-success ml-2" @click="printToWord"
              :disabled="isExporting"> <i class="fas" :class="isExporting ? 'fa-spinner fa-spin' : 'fa-file-word'"></i>
              {{ isExporting ? 'Đang xuất...' : 'Xuất Word' }}
            </button>
            <button v-if="allReportData.length > 0" type="button" class="btn btn-info ml-2" @click="exportToExcel"
              :disabled="isExportingExcel">
              <i class="fas" :class="isExportingExcel ? 'fa-spinner fa-spin' : 'fa-file-excel'"></i>
              {{ isExportingExcel ? 'Đang xuất...' : 'Xuất Excel' }}
            </button>
          </div>
        </div>

        <div class="card mt-3">
          <div class="card-header border-0">
            <h3 class="card-title text-success font-weight-bold">
              <i class="fas fa-chart-bar"></i> {{ reportTitle }}
            </h3>
            <div class="card-tools">
              <span class="badge badge-success" v-if="allReportData.length > 0">{{ allReportData.length }} dòng</span>
            </div>
          </div>

          <div class="card-body table-responsive p-0">
            <table class="table table-bordered table-striped table-hover text-nowrap">
              <thead class="bg-light">
                <tr>
                  <th class="text-center" width="50">STT</th>
                  <th>Mã SP</th>
                  <th>Tên Sản Phẩm </th>
                  <th class="text-center">ĐVT</th>
                  <th class="text-right">Tồn Đầu</th>
                  <th class="text-right">Nhập</th>
                  <th class="text-right">Xuất</th>
                  <th class="text-right">Tồn Cuối</th>
                  <th class="text-right">Giá BQ</th>
                  <th class="text-right">Thành Tiền</th>
                </tr>
              </thead>
              <tbody>
                <template v-if="loading">
                  <tr v-for="n in 5" :key="`skel-${n}`">
                    <td v-for="m in 10" :key="m">
                      <div class="skeleton-loader"></div>
                    </td>
                  </tr>
                </template>

                <template v-else>
                  <tr v-if="paginatedData.length === 0">
                    <td colspan="10" class="text-center py-4 text-muted">
                      <i class="fas fa-inbox fa-2x mb-2"></i><br>
                      Không có dữ liệu phát sinh trong khoảng thời gian này.
                    </td>
                  </tr>

                  <tr v-for="(item, index) in paginatedData" :key="index">
                    <td class="text-center">
                      {{ (pagination.page * pagination.size) + index + 1 }}
                    </td>
                    <td class="text-primary font-weight-bold font-monospace">{{ item.maSP }}</td>

                    <td>{{ formatProductName(item.tenSP) }}</td>

                    <td class="text-center">{{ item.donvitinh }}</td>

                    <td class="text-right">{{ item.tonDau }}</td>
                    <td class="text-right text-success">+{{ item.nhapTrong }}</td>
                    <td class="text-right text-danger">-{{ item.xuatTrong }}</td>

                    <td class="text-right font-weight-bold bg-warning bg-opacity-25">{{ item.tonCuoi }}</td>

                    <td class="text-right">{{ formatCurrency(item.giaBQ) }}</td>
                    <td class="text-right font-weight-bold text-primary">{{ formatCurrency(item.thanhTien) }}</td>
                  </tr>
                </template>
              </tbody>
            </table>
          </div>

          <div class="bg-light border-top py-1" v-if="allReportData.length > 0 && !loading"
            style="border-bottom-left-radius: .25rem; border-bottom-right-radius: .25rem;">
            <div class="d-flex justify-content-around text-center align-items-center"
              style="font-size: 13px; min-height: 40px;">
              <div class="flex-fill border-right">
                <span class="text-muted mr-1">Tồn Đầu:</span>
                <span class="font-weight-bold">{{ grandTotal.tdk }}</span>
              </div>
              <div class="flex-fill border-right">
                <span class="text-success mr-1">Nhập:</span>
                <span class="font-weight-bold">{{ grandTotal.ntk }}</span>
              </div>
              <div class="flex-fill border-right">
                <span class="text-danger mr-1">Xuất:</span>
                <span class="font-weight-bold">{{ grandTotal.xtk }}</span>
              </div>
              <div class="flex-fill border-right">
                <span class="text-dark mr-1">Tồn Cuối:</span>
                <span class="font-weight-bold text-warning">{{ grandTotal.tck }}</span>
              </div>
              <div class="flex-fill">
                <span class="text-primary mr-1">Tổng Tiền:</span>
                <span class="font-weight-bold text-primary">{{ formatCurrency(grandTotal.tien) }}</span>
              </div>
            </div>
          </div>

          <div class="d-flex justify-content-center mt-2 px-3 pb-3" v-if="pagination.totalPages > 1">
            <ul class="pagination pagination-sm m-0">
              <li class="page-item" :class="{ disabled: pagination.page === 0 }">
                <a class="page-link" href="#" @click.prevent="changePage(pagination.page - 1)">« Trước</a>
              </li>
              <li v-for="(page, index) in visiblePages" :key="index" class="page-item"
                :class="{ active: page === pagination.page + 1, disabled: page === '...' }">

                <a class="page-link" href="#" @click.prevent="page !== '...' ? changePage(page - 1) : null">
                  {{ page }}
                </a>
              </li>

              <li class="page-item" :class="{ disabled: pagination.page >= pagination.totalPages - 1 }">
                <a class="page-link" href="#" @click.prevent="changePage(pagination.page + 1)">Sau »</a>
              </li>
            </ul>
          </div>
        </div>

      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import api from '@/utils/axios';
import { saveAs } from "file-saver";
import PizZip from "pizzip";
import Docxtemplater from "docxtemplater";
import ExcelJS from 'exceljs';

// --- CẤU HÌNH API ---
const API_URL = '/thong-ke/xuat-nhap-ton';

// --- STATE ---
const today = new Date();
const firstDay = new Date(today.getFullYear(), today.getMonth(), 1);
const isExporting = ref(false);
const isAdmin = ref(false);
const isChotSoState = ref(true);
const isExportingExcel = ref(false);

const allReportData = ref([]);
const loading = ref(false);
const currentTenKho = ref('Tất cả các kho');

// --- TÍNH TOÁN DỮ LIỆU PHÂN TRANG (Client-side) ---
const pagination = reactive({
  page: 0,
  size: 20,
  total: 0,
  totalPages: 0
});

const paginatedData = computed(() => {
  const start = pagination.page * pagination.size;
  const end = start + pagination.size;
  return allReportData.value.slice(start, end);
});

const grandTotal = computed(() => {
  return allReportData.value.reduce((acc, item) => {
    acc.tdk += item.tonDau || 0;
    acc.ntk += item.nhapTrong || 0;
    acc.xtk += item.xuatTrong || 0;
    acc.tck += item.tonCuoi || 0;
    acc.tien += item.thanhTien || 0;
    return acc;
  }, { tdk: 0, ntk: 0, xtk: 0, tck: 0, tien: 0 });
});

const visiblePages = computed(() => {
  const total = pagination.totalPages;
  if (total === 0) return [];
  const current = pagination.page + 1;
  const delta = 2;
  const range = [];
  const rangeWithDots = [];
  let l;

  for (let i = 1; i <= total; i++) {
    if (i === 1 || i === total || (i >= current - delta && i <= current + delta)) {
      range.push(i);
    }
  }

  for (let i of range) {
    if (l) {
      if (i - l === 2) rangeWithDots.push(l + 1);
      else if (i - l !== 1) rangeWithDots.push('...');
    }
    rangeWithDots.push(i);
    l = i;
  }
  return rangeWithDots;
});

const changePage = (newPage) => {
  if (newPage < 0 || newPage >= pagination.totalPages) return;
  pagination.page = newPage;
};

const filters = reactive({
  startDate: firstDay.toISOString().substring(0, 10),
  endDate: today.toISOString().substring(0, 10),
  warehouseId: 0, // Mặc định 0 là Tất cả kho
  statusId: 0     // ĐÃ SỬA: Mặc định 0 là "Tất cả" trạng thái theo Backend mới
});

const khoList = ref([]);

const statusList = ref([
  { id: 0, name: 'Tất cả' },
  { id: 1, name: 'Bình thường' }, // Giao diện hiện "Bình thường" cho người dùng dễ chọn, in ra nó vẫn tự ẩn
  { id: 2, name: 'New' },
  { id: 3, name: 'Like New' },
  { id: 4, name: 'Hỏng' },
  { id: 5, name: 'Xác' },
  { id: 6, name: 'Thu hồi' },
  { id: 7, name: 'Nhập Khẩu' }
]);

const reportTitle = computed(() => {
  return `Báo cáo: ${currentTenKho.value} (${formatDateString(filters.startDate)} - ${formatDateString(filters.endDate)})`;
});

// --- LOGIC PHÂN QUYỀN ---
const setupPhanQuyen = () => {
  const role = localStorage.getItem('userRole');
  let userMaKho = localStorage.getItem('maKho') || localStorage.getItem('userMaKho');

  if (!userMaKho) {
    try {
      const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
      userMaKho = userInfo.maKho;
    } catch (e) { }
  }

  if (role === 'ADMIN' || role === 'ROLE_ADMIN') {
    isAdmin.value = true;
  } else {
    isAdmin.value = false;
    const myKho = userMaKho ? parseInt(userMaKho) : 0;
    filters.warehouseId = myKho;
  }
};

const loadKho = async () => {
  try {
    const res = await api.get('/kho');
    khoList.value = res.data;
  } catch (e) {
    console.error("Lỗi tải kho:", e);
  }
};

const showFirstYearWarning = () => {
  alert("LƯU Ý:\nĐây là năm đầu tiên sử dụng phần mềm, hệ thống chưa có dữ liệu chốt sổ của năm trước.\n\nDo đó, Tồn Đầu Kỳ sẽ được mặc định là 0.");
};

const handleSearch = () => {
  pagination.page = 0;
  fetchInventoryReport();
};

const formatProductName = (name) => {
  if (!name) return '';
  let formattedName = name.toString();

  // 1. Dọn dẹp các trạng thái không cần hiển thị (Bình thường, Chưa xác định)
  formattedName = formattedName.replace(/\s*-\s*Bình thường/gi, '');
  formattedName = formattedName.replace(/\s*-\s*Chưa xác định/gi, '');

  // 2. Chuẩn hóa mọi biến thể của từ "Mới" về đúng chuẩn " - New"
  formattedName = formattedName.replace(/\s*-\s*Mới\s*\(New\)/gi, ' - New');
  formattedName = formattedName.replace(/\s*-\s*Mới/gi, ' - New');
  formattedName = formattedName.replace(/\s*-\s*New/gi, ' - New'); // Ép lại khoảng trắng cho đều

  // 3. QUAN TRỌNG: Cắt bỏ dấu gạch ngang trơ trọi ở cuối chuỗi 
  // (Phòng hờ lỗi Backend nối chuỗi trạng thái rỗng ' ' của ID 1 thành "Tên Máy - ")
  formattedName = formattedName.replace(/\s*-\s*$/g, '');

  return formattedName.trim();
};

const fetchInventoryReport = async () => {
  if (!filters.startDate || !filters.endDate) {
    alert("Vui lòng chọn đầy đủ Từ ngày và Đến ngày");
    return;
  }

  const start = new Date(filters.startDate);
  const end = new Date(filters.endDate);

  if (start > end) {
    alert("Ngày bắt đầu không được lớn hơn ngày kết thúc");
    return;
  }

  loading.value = true;
  allReportData.value = [];

  try {
    const yearToCheck = start.getFullYear();

    const checkRes = await api.get('/thong-ke/check-chot-so', {
      params: {
        nam: yearToCheck,
        maKho: filters.warehouseId
      }
    });

    isChotSoState.value = checkRes.data;

    const response = await api.get(API_URL, {
      params: {
        maKho: filters.warehouseId,
        tuNgay: filters.startDate,
        denNgay: filters.endDate,
        loaiLoc: filters.statusId,
        page: 0,
        size: 999999
      }
    });

    const data = response.data;
    currentTenKho.value = data.tenKho;
    allReportData.value = data.danhSachChiTiet || [];

    pagination.total = allReportData.value.length;
    pagination.totalPages = Math.ceil(pagination.total / pagination.size);

  } catch (error) {
    console.error("Lỗi:", error);
    if (error.response && error.response.status === 400) {
      alert("Lỗi dữ liệu: " + error.response.data);
    } else {
      alert("Có lỗi xảy ra khi tải báo cáo.");
    }
  } finally {
    loading.value = false;
  }
};

const loadFile = async (url) => {
  const response = await fetch(url);
  if (!response.ok) throw new Error(`Lỗi tải file mẫu: ${url}`);
  return await response.arrayBuffer();
};

const printToWord = async () => {
  if (isExporting.value) return;
  isExporting.value = true;

  try {
    const dataToExport = allReportData.value;
    if (dataToExport.length === 0) {
      alert("Không có dữ liệu để xuất file.");
      return;
    }

    const content = await loadFile("/File_Mau_BaoCaoTonKho.docx");
    const zip = new PizZip(content);
    const doc = new Docxtemplater(zip, { paragraphLoop: true, linebreaks: true });

    const now = new Date();
    const dd = String(now.getDate()).padStart(2, '0');
    const mm = String(now.getMonth() + 1).padStart(2, '0');
    const yyyy = now.getFullYear();
    let hours = now.getHours();
    const minutes = String(now.getMinutes()).padStart(2, '0');
    const ampm = hours >= 12 ? 'PM' : 'AM';
    hours = hours % 12; hours = hours ? hours : 12;

    const dataToRender = {
      ngayBatDau: formatDateString(filters.startDate),
      ngayKetThuc: formatDateString(filters.endDate),
      tenKho: currentTenKho.value,

      p: dataToExport.map((item, index) => ({
        stt: index + 1,
        ma: item.maSP,
        ten: formatProductName(item.tenSP),
        dvt: item.donvitinh,
        tdk: item.tonDau,
        ntk: item.nhapTrong,
        xtk: item.xuatTrong,
        tck: item.tonCuoi,
        gia: formatCurrency(item.giaBQ),
        tien: formatCurrency(item.thanhTien)
      })),

      sumTDK: grandTotal.value.tdk,
      sumNTK: grandTotal.value.ntk,
      sumXTK: grandTotal.value.xtk,
      sumTCK: grandTotal.value.tck,
      sumTien: formatCurrency(grandTotal.value.tien),

      d: dd, m: mm, y: yyyy, h: String(hours).padStart(2, '0'), ph: minutes, ampm: ampm
    };

    doc.render(dataToRender);
    const out = doc.getZip().generate({ type: "blob", mimeType: "application/vnd.openxmlformats-officedocument.wordprocessingml.document" });

    let formattedEndDate = filters.endDate;
    if (filters.endDate && filters.endDate.includes('-')) {
      const parts = filters.endDate.split('-');
      if (parts.length === 3) formattedEndDate = `${parts[2]}-${parts[1]}-${parts[0]}`;
    }

    saveAs(out, `Báo Cáo Tồn Kho - ${currentTenKho.value} _ ${formattedEndDate}.docx`);

  } catch (error) {
    console.error(error);
    alert("Lỗi xuất file Word: " + error.message);
  } finally {
    isExporting.value = false;
  }
};

const exportToExcel = async () => {
  if (isExportingExcel.value) return;
  isExportingExcel.value = true;

  try {
    const dataToExport = allReportData.value;
    if (dataToExport.length === 0) {
      alert("Không có dữ liệu để xuất file.");
      return;
    }

    const workbook = new ExcelJS.Workbook();
    const sheet = workbook.addWorksheet('Báo Cáo Tồn Kho');

    sheet.getCell('A1').value = 'BÁO CÁO XUẤT NHẬP TỒN';
    sheet.getCell('A1').font = { size: 16, bold: true };
    sheet.mergeCells('A1:J1');

    sheet.getCell('A2').value = 'Ngày bắt đầu:';
    sheet.getCell('C2').value = formatDateString(filters.startDate);

    sheet.getCell('A3').value = 'Ngày kết thúc:';
    sheet.getCell('C3').value = formatDateString(filters.endDate);

    sheet.getCell('A4').value = `Kho: ${currentTenKho.value}`;
    sheet.getCell('A4').font = { bold: true };

    const headerRow = sheet.addRow([
      'Stt', 'Mã sản phẩm', 'Tên sản phẩm', 'ĐVT',
      'TĐK', 'NTK', 'XTK', 'TCK', 'Giá/BQ', 'Thành tiền'
    ]);
    headerRow.font = { bold: true };
    headerRow.alignment = { vertical: 'middle', horizontal: 'center' };

    sheet.columns = [
      { key: 'stt', width: 5 },
      { key: 'maSP', width: 20 },
      { key: 'tenSP', width: 45 },
      { key: 'dvt', width: 8 },
      { key: 'tdk', width: 10 },
      { key: 'ntk', width: 10 },
      { key: 'xtk', width: 10 },
      { key: 'tck', width: 10 },
      { key: 'gia', width: 15 },
      { key: 'tien', width: 18 }
    ];

    dataToExport.forEach((item, index) => {
      const row = sheet.addRow([
        index + 1,
        item.maSP,
        formatProductName(item.tenSP),
        item.donvitinh,
        item.tonDau || 0,
        item.nhapTrong || 0,
        item.xuatTrong || 0,
        item.tonCuoi || 0,
        item.giaBQ || 0,
        item.thanhTien || 0
      ]);

      for (let i = 5; i <= 10; i++) {
        row.getCell(i).alignment = { horizontal: 'right' };
      }
    });

    const totalRow = sheet.addRow(['Tổng', '', '', '', grandTotal.value.tdk, grandTotal.value.ntk, grandTotal.value.xtk, grandTotal.value.tck, '', grandTotal.value.tien]);
    totalRow.font = { bold: true };
    sheet.mergeCells(`A${totalRow.number}:D${totalRow.number}`);

    const sumQtyRow = sheet.addRow([`Tổng số lượng: ${grandTotal.value.tck} Cái`]);
    sumQtyRow.font = { bold: true };
    sheet.mergeCells(`A${sumQtyRow.number}:J${sumQtyRow.number}`);

    const now = new Date();
    const dd = String(now.getDate()).padStart(2, '0');
    const mm = String(now.getMonth() + 1).padStart(2, '0');
    const yyyy = now.getFullYear();
    const currentDateStr = `${dd}-${mm}-${yyyy}`;

    const dateRow = sheet.addRow([`Ngày in ${currentDateStr}`]);
    dateRow.alignment = { horizontal: 'right' };
    sheet.mergeCells(`A${dateRow.number}:J${dateRow.number}`);

    sheet.eachRow((row, rowNumber) => {
      if (rowNumber >= 5 && rowNumber <= totalRow.number) {
        row.eachCell((cell) => {
          cell.border = {
            top: { style: 'thin' },
            left: { style: 'thin' },
            bottom: { style: 'thin' },
            right: { style: 'thin' }
          };
        });
      }
    });

    let formattedEndDate = filters.endDate;
    if (filters.endDate && filters.endDate.includes('-')) {
      const parts = filters.endDate.split('-');
      if (parts.length === 3) {
        formattedEndDate = `${parts[2]}-${parts[1]}-${parts[0]}`;
      }
    }

    const fileName = `Báo Cáo Xuất Nhập Tồn _ ${currentTenKho.value.replace(/\s+/g, '')} _ ${formattedEndDate}.xlsx`;

    const buffer = await workbook.xlsx.writeBuffer();
    const blob = new Blob([buffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
    saveAs(blob, fileName);

  } catch (error) {
    console.error("Lỗi xuất file Excel:", error);
    alert("Có lỗi xảy ra khi xuất file Excel.");
  } finally {
    isExportingExcel.value = false;
  }
};

const formatCurrency = (value) => {
  if (!value || isNaN(value)) return '0';
  return new Intl.NumberFormat('vi-VN', { style: 'decimal', decimal: 'VND' }).format(value);
};

const formatDateString = (dateStr) => {
  if (!dateStr) return "...";
  const parts = dateStr.split('-');
  if (parts.length !== 3) return dateStr;
  return `${parts[2]}/${parts[1]}/${parts[0]}`;
}

onMounted(async () => {
  const token = localStorage.getItem('token');
  if (!token) return;
  setupPhanQuyen();
  await loadKho();
});
</script>

<style scoped>
.skeleton-loader {
  width: 100%;
  height: 1.2em;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 4px;
}

@keyframes shimmer {
  0% {
    background-position: 200% 0;
  }

  100% {
    background-position: -200% 0;
  }
}

.card-body.table-responsive {
  max-height: 75vh;
  overflow-y: auto;
  border-bottom: 1px solid #dee2e6;
  -webkit-overflow-scrolling: touch;
}

.table thead th {
  position: sticky;
  top: 0;
  background-color: #f4f6f9;
  z-index: 10;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

@media (max-width: 768px) {
  .card-body .row>.col-12 {
    flex: 0 0 50%;
    max-width: 50%;
  }

  .form-control {
    font-size: 13px;
    padding: 0.25rem 0.5rem;
    height: calc(1.8125rem + 2px);
  }

  .form-group label {
    font-size: 12px;
    margin-bottom: 2px;
  }

  .card-footer {
    display: flex;
    flex-direction: column;
    gap: 10px;
  }

  .card-footer .btn {
    width: 100%;
    margin-left: 0 !important;
  }

  .table {
    font-size: 13px;
  }

  .table th,
  .table td {
    padding: 8px 6px;
  }

  .pagination .page-link {
    padding: 0.25rem 0.5rem;
    font-size: 12px;
  }
}

@media (min-width: 768px) and (max-width: 1024px) {
  .table {
    font-size: 13px;
  }
}
</style>