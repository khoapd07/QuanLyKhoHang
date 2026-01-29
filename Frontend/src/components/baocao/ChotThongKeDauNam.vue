<template>
  <div class="content-wrapper">
    <div class="content-header">
      <div class="container-fluid">
        <div class="row mb-2">
          <div class="col-sm-6">
            <h1 class="m-0">Quản Lý Chốt Tồn Kho</h1>
          </div>
        </div>
      </div>
    </div>

    <section class="content">
      <div class="container-fluid">
        
        <div class="card card-primary card-outline card-tabs">
          <div class="card-header p-0 pt-1 border-bottom-0">
            <ul class="nav nav-tabs" id="custom-tabs-three-tab" role="tablist">
              <li class="nav-item">
                <a class="nav-link" :class="{ active: activeTab === 'action' }" @click="activeTab = 'action'" href="#" role="tab">
                  <i class="fas fa-edit"></i> Thực hiện chốt sổ
                </a>
              </li>
              <li class="nav-item">
                <a class="nav-link" :class="{ active: activeTab === 'history' }" @click="activeTab = 'history'" href="#" role="tab">
                  <i class="fas fa-history"></i> Xem dữ liệu đã chốt
                </a>
              </li>
            </ul>
          </div>

          <div class="card-body">
            <div class="tab-content">
              
              <div class="tab-pane fade show" :class="{ active: activeTab === 'action' }">
                <div class="alert alert-warning">
                  <i class="fas fa-exclamation-triangle"></i> <strong>Cảnh báo:</strong> Hành động này sẽ tính toán lại tồn kho và <strong>GHI ĐÈ</strong> lên dữ liệu Tồn Đầu Kỳ của năm sau.
                </div>
                <div class="row">
                  <div class="col-lg-6 col-md-6">
                    <div class="form-group">
                      <label>Năm Cần Chốt (Tính từ 1/1 đến 31/12)</label>
                      <input type="number" class="form-control" v-model="filters.nam" />
                    </div>
                  </div>
                  <div class="col-lg-6 col-md-6">
                    <div class="form-group">
                      <label>Kho áp dụng</label>
                      <select class="form-control" v-model="filters.warehouseId">
                        <option v-for="kho in khoList" :key="kho.id" :value="kho.id">{{ kho.name }}</option>
                      </select>
                    </div>
                  </div>
                </div>
              </div>

              <div class="tab-pane fade show" :class="{ active: activeTab === 'history' }">
                <div class="alert alert-info">
                  <i class="fas fa-info-circle"></i> Xem lại dữ liệu Tồn Đầu Kỳ đã được lưu trong hệ thống (Không làm thay đổi dữ liệu).
                </div>
                <div class="row">
                  <div class="col-lg-4 col-md-6">
                    <div class="form-group">
                      <label>Xem kết quả chốt của Năm</label>
                      <input type="number" class="form-control" v-model="filters.nam" />
                      <small class="text-muted">Nhập 2024 -> Xem Tồn Đầu 2025</small>
                    </div>
                  </div>
                  <div class="col-lg-4 col-md-6">
                    <div class="form-group">
                      <label>Kho áp dụng</label>
                      <select class="form-control" v-model="filters.warehouseId">
                        <option v-for="kho in khoList" :key="kho.id" :value="kho.id">{{ kho.name }}</option>
                      </select>
                    </div>
                  </div>
                  <div class="col-lg-4 col-md-6">
                    <div class="form-group">
                      <label>Trạng thái</label>
                      <select class="form-control" v-model="filters.statusId" @change="xemLichSu">
                        <option :value="0">-- Tất cả --</option>
                        <option v-for="st in statusList" :key="st.id" :value="st.id">{{ st.name }}</option>
                      </select>
                    </div>
                  </div>
                </div>
              </div>

            </div>
          </div>
          
          <div class="card-footer d-flex justify-content-start align-items-center bg-white border-top">
            <div>
               <button v-if="activeTab === 'action'" type="button" class="btn btn-warning font-weight-bold shadow-sm px-4 mr-2" style="margin-right: 10px;" @click="thucHienChotSo" :disabled="loading">
                  <i class="fas fa-save"></i> Chốt Sổ
               </button>

               <button v-if="activeTab === 'history'" type="button" class="btn btn-primary font-weight-bold shadow-sm px-4 mr-2"style="margin-right: 10px;" @click="xemLichSu" :disabled="loading">
                  <i class="fas fa-search"></i> Xem Ngay
               </button>
            </div>

            <div>
                <button v-if="reportData.length > 0" type="button" class="btn btn-info shadow-sm" @click="printToWord">
                  <i class="fas fa-file-word"></i> Xuất Biên Bản (Word)
                </button>
            </div>
          </div>
        </div>


        <div class="card mt-4" v-if="reportData.length > 0 || loading">
          <div class="card-header border-0">
            <h3 class="card-title font-weight-bold" :class="activeTab === 'action' ? 'text-danger' : 'text-primary'">
                <i class="fas" :class="activeTab === 'action' ? 'fa-bolt' : 'fa-history'"></i> 
                {{ activeTab === 'action' ? 'KẾT QUẢ VỪA CHỐT:' : 'DỮ LIỆU ĐANG LƯU TRỮ:' }} 
                TỒN ĐẦU NĂM {{ parseInt(filters.nam) + 1 }}
            </h3>
            <div class="card-tools">
                <span class="badge" :class="activeTab === 'action' ? 'badge-danger' : 'badge-primary'">{{ reportData.length }} bản ghi</span>
            </div>
          </div>
          
          <div class="card-body table-responsive p-0">
            <table class="table table-bordered table-striped table-hover">
              <thead class="bg-light">
                <tr class="text-center">
                  <th style="width: 50px">STT</th>
                  <th>Mã SP</th>
                  <th>Tên Sản Phẩm</th>
                  <th>ĐVT</th>
                  <th>Tồn Đầu</th>
                  <th>Nhập</th>
                  <th>Xuất</th>
                  <th>Tồn Cuối</th>
                  <th>Giá BQ</th>
                  <th>Thành Tiền</th>
                </tr>
              </thead>
              <tbody>
                <template v-if="loading">
                  <tr v-for="n in 5" :key="`skel-${n}`">
                    <td v-for="m in 10" :key="m"><div class="skeleton-loader"></div></td>
                  </tr>
                </template>

                <template v-else>
                  <tr v-if="reportData.length === 0">
                     <td colspan="10" class="text-center py-4 text-muted">Chưa có dữ liệu.</td>
                  </tr>

                  <tr v-for="(item, index) in reportData" :key="index">
                    <td class="text-center">{{ index + 1 }}</td>
                    <td class="font-weight-bold text-primary">{{ item.maSP }}</td>
                    <td>{{ item.tenSP }}</td>
                    <td class="text-center">{{ item.donvitinh }}</td>
                    
                    <td class="text-right">{{ item.tonDau }}</td>
                    <td class="text-right text-success">{{ item.nhapTrong }}</td>
                    <td class="text-right text-danger">{{ item.xuatTrong }}</td>
                    
                    <td class="text-right font-weight-bold" style="background-color: #f8f9fa">
                        {{ item.tonCuoi }}
                    </td>
                    
                    <td class="text-right">{{ formatCurrency(item.giaBQ) }}</td>
                    <td class="text-right font-weight-bold text-success">
                        {{ formatCurrency(item.thanhTien) }}
                    </td>
                  </tr>

                  <tr class="bg-warning font-weight-bold">
                      <td colspan="4" class="text-center">TỔNG CỘNG</td>
                      <td class="text-right">{{ sumTotals.tdk }}</td>
                      <td class="text-right">{{ sumTotals.ntk }}</td>
                      <td class="text-right">{{ sumTotals.xtk }}</td>
                      <td class="text-right">{{ sumTotals.tck }}</td>
                      <td></td>
                      <td class="text-right">{{ formatCurrency(sumTotals.tien) }}</td>
                  </tr>
                </template>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue';
import axios from 'axios';
import { saveAs } from "file-saver";
import PizZip from "pizzip";
import Docxtemplater from "docxtemplater";

const API_BASE = 'http://localhost:8080/api/thong-ke'; 

// --- STATE ---
const activeTab = ref('action'); // 'action' | 'history'
const filters = reactive({
  nam: new Date().getFullYear() - 1, 
  warehouseId: 1,
  statusId: 0, // [MỚI] Trạng thái (0: Tất cả)
});

// Danh sách trạng thái (Khớp ID Database)
const statusList = ref([
    { id: 1, name: 'Mới' },
    { id: 2, name: 'Like New' },
    { id: 3, name: 'Hỏng' },
    { id: 4, name: 'Xác' },
    { id: 5, name: 'Thu hồi' }
]);

const khoList = ref([
    { id: 1, name: 'Kho Tổng Hà Nội' },
    { id: 2, name: 'Kho Chi Nhánh HCM' }
]);

const reportData = ref([]);
const loading = ref(false);
const currentTenKho = ref('');

// --- COMPUTED: Tính tổng full cột ---
const sumTotals = computed(() => {
    return reportData.value.reduce((acc, item) => {
        acc.tdk += item.tonDau || 0;
        acc.ntk += item.nhapTrong || 0;
        acc.xtk += item.xuatTrong || 0;
        acc.tck += item.tonCuoi || 0;
        acc.tien += (typeof item.thanhTien === 'number' ? item.thanhTien : 0);
        return acc;
    }, { tdk: 0, ntk: 0, xtk: 0, tck: 0, tien: 0 });
});

// --- API: 1. THỰC HIỆN CHỐT (POST) ---
const thucHienChotSo = async () => {
  const namChot = parseInt(filters.nam) || new Date().getFullYear();
  const namSau = namChot + 1;

  if(!confirm(`⚠️ CẢNH BÁO: CHỐT SỔ NĂM ${namChot}\nDữ liệu sẽ được ghi vào Tồn Đầu năm ${namSau}.\nBạn có chắc chắn muốn tiếp tục?`)) {
      return;
  }

  loading.value = true;
  reportData.value = [];

  try {
    const response = await axios.post(`${API_BASE}/chot-so`, null, {
      params: { nam: namChot, maKho: filters.warehouseId }
    });
    const data = response.data;
    currentTenKho.value = data.tenKho;
    reportData.value = data.danhSachChiTiet;
    alert("Đã chốt sổ thành công!");
  } catch (error) {
    console.error("Lỗi:", error);
    alert("Lỗi: " + (error.response?.data || error.message));
  } finally {
    loading.value = false;
  }
};

// --- API: 2. XEM LỊCH SỬ (GET - Có lọc theo trạng thái) ---
const xemLichSu = async () => {
  loading.value = true;
  reportData.value = [];
  
  const namChot = parseInt(filters.nam);
  const namSau = namChot + 1;

  try {
    // Gọi API Xem báo cáo
    // Truyền loaiLoc = filters.statusId để Backend hoặc SQL lọc
    const response = await axios.get(`${API_BASE}/xuat-nhap-ton`, {
      params: {
        maKho: filters.warehouseId,
        tuNgay: `${namSau}-01-01`,
        denNgay: `${namSau}-01-01`, 
        loaiLoc: filters.statusId // [MỚI] Truyền trạng thái lên
      }
    });

    const data = response.data;
    currentTenKho.value = data.tenKho;
    reportData.value = data.danhSachChiTiet;

  } catch (error) {
    console.error("Lỗi:", error);
    alert("Không thể tải dữ liệu: " + (error.response?.data || error.message));
  } finally {
    loading.value = false;
  }
};

// --- HELPERS ---
const formatCurrency = (value) => {
  if (!value || isNaN(value)) return '0 ₫';
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value);
};

const loadFile = async (url) => {
    const response = await fetch(url);
    if (!response.ok) throw new Error(`Lỗi tải file: ${url}`);
    return await response.arrayBuffer();
};

// --- EXPORT WORD (Full cột) ---
const printToWord = async () => {
  if (!reportData.value.length) return alert("Không có dữ liệu.");

  try {
      const content = await loadFile("/File_Mau_BaoCaoChotSoNam.docx");
      const zip = new PizZip(content);
      const doc = new Docxtemplater(zip, { paragraphLoop: true, linebreaks: true });

      const now = new Date();
      const dd = String(now.getDate()).padStart(2,'0');
      const mm = String(now.getMonth()+1).padStart(2,'0');
      const yyyy = now.getFullYear();

      const dataToRender = {
          nam: filters.nam,
          namSau: parseInt(filters.nam) + 1,
          tenKho: currentTenKho.value,
          tieuDe: activeTab.value === 'action' ? 'BIÊN BẢN CHỐT TỒN KHO' : 'BÁO CÁO SỐ LIỆU ĐÃ CHỐT',
          
          sumTDK: new Intl.NumberFormat('vi-VN').format(sumTotals.value.tdk),
          sumNTK: new Intl.NumberFormat('vi-VN').format(sumTotals.value.ntk),
          sumXTK: new Intl.NumberFormat('vi-VN').format(sumTotals.value.xtk),
          sumTCK: new Intl.NumberFormat('vi-VN').format(sumTotals.value.tck),
          sumTien: formatCurrency(sumTotals.value.tien),
          
          d: dd, m: mm, y: yyyy,

          p: reportData.value.map((item, index) => ({
              stt: index + 1,
              ma: item.maSP,
              ten: item.tenSP,
              dvt: item.donvitinh,
              tdk: item.tonDau,
              ntk: item.nhapTrong,
              xtk: item.xuatTrong,
              tck: item.tonCuoi,
              gia: new Intl.NumberFormat('vi-VN').format(item.giaBQ),
              tien: formatCurrency(item.thanhTien)
          }))
      };

      doc.render(dataToRender);
      const out = doc.getZip().generate({
          type: "blob",
          mimeType: "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
      });
      saveAs(out, `ChotSo_${filters.nam}.docx`);

  } catch (error) {
      alert("Lỗi xuất file: " + error.message);
  }
};
</script>

<style scoped>
.skeleton-loader {
  width: 100%; height: 1.2em;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite; border-radius: 4px;
}
@keyframes shimmer { 0% { background-position: 200% 0; } 100% { background-position: -200% 0; } }
.nav-tabs .nav-link.active {
    font-weight: bold;
}
</style>