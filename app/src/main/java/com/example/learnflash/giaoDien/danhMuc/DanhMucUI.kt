package com.example.learnflash.giaoDien.danhMuc

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.learnflash.duLieu.local.thucThe.DanhMuc

// Thành phần giao diện cấu thành màn hình Quản lý Danh mục từ vựng
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DanhMucUI(
    viewModel: DanhMucViewModel,
    quayLai: () -> Unit
) {
    // Đọc các StateFlow từ ViewModel
    val danhSachDanhMuc by viewModel.danhSachDanhMuc.collectAsState()
    val hienHopThoaiThem by viewModel.hienHopThoaiThem.collectAsState()
    val danhMucCanXoa by viewModel.danhMucCanXoa.collectAsState()
    val thongBao by viewModel.thongBao.collectAsState()
    val tenDanhMucMoi by viewModel.tenDanhMucMoi.collectAsState()
    val moTaDanhMucMoi by viewModel.moTaDanhMucMoi.collectAsState()

    // Khởi tạo SnackbarHostState để hiển thị thông báo kết quả thao tác
    val snackbarHostState = remember { SnackbarHostState() }

    // Theo dõi StateFlow thông báo và kích hoạt SnackBar khi có nội dung mới
    LaunchedEffect(thongBao) {
        if (thongBao.isNotEmpty()) {
            snackbarHostState.showSnackbar(thongBao)
            viewModel.daHienThiThongBao()
        }
    }

    // Khung giao diện Scaffold với TopAppBar và FAB thêm danh mục
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Danh mục từ vựng") },
                navigationIcon = {
                    // Nút điều hướng quay lại màn hình trước
                    IconButton(onClick = quayLai) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        },
        floatingActionButton = {
            // Nút nổi kích hoạt hộp thoại thêm danh mục mới
            FloatingActionButton(onClick = { viewModel.moHopThoaiThem() }) {
                Icon(Icons.Default.Add, contentDescription = "Thêm danh mục")
            }
        },
        snackbarHost = {
            // Vùng hiển thị thông báo SnackBar
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        // Danh sách cuộn LazyColumn hiển thị các danh mục
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }
            items(danhSachDanhMuc) { danhMuc ->
                // Thành phần giao diện con hiển thị một dòng thông tin danh mục
                ItemDanhMuc(
                    danhMuc = danhMuc,
                    onXoa = { viewModel.yeuCauXoaDanhMuc(danhMuc) }
                )
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    // Hộp thoại nhập liệu thêm danh mục mới — hiển thị khi hienHopThoaiThem = true
    if (hienHopThoaiThem) {
        AlertDialog(
            onDismissRequest = { viewModel.dongHopThoaiThem() },
            title = { Text("Thêm danh mục mới") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Trường nhập tên danh mục (bắt buộc)
                    OutlinedTextField(
                        value = tenDanhMucMoi,
                        onValueChange = { viewModel.capNhatTenDanhMuc(it) },
                        label = { Text("Tên danh mục *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    // Trường nhập mô tả danh mục (tùy chọn)
                    OutlinedTextField(
                        value = moTaDanhMucMoi,
                        onValueChange = { viewModel.capNhatMoTaDanhMuc(it) },
                        label = { Text("Mô tả (tùy chọn)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                // Nút xác nhận lưu danh mục mới
                Button(onClick = { viewModel.luuDanhMucMoi() }) {
                    Text("Lưu")
                }
            },
            dismissButton = {
                // Nút hủy đóng hộp thoại không lưu
                TextButton(onClick = { viewModel.dongHopThoaiThem() }) {
                    Text("Hủy")
                }
            }
        )
    }

    // Hộp thoại xác nhận xóa danh mục — hiển thị khi danhMucCanXoa khác null
    if (danhMucCanXoa != null) {
        AlertDialog(
            onDismissRequest = { viewModel.huyXoaDanhMuc() },
            title = { Text("Xác nhận xóa") },
            text = { Text("Xóa danh mục \"${danhMucCanXoa?.ten}\"? Thao tác không thể hoàn tác.") },
            confirmButton = {
                Button(onClick = { viewModel.xacNhanXoaDanhMuc() }) {
                    Text("Xóa")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.huyXoaDanhMuc() }) {
                    Text("Hủy")
                }
            }
        )
    }
}

// Thành phần Composable hiển thị một dòng thông tin danh mục trong LazyColumn
@Composable
fun ItemDanhMuc(danhMuc: DanhMuc, onXoa: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Biểu tượng thư mục phân biệt danh mục mặc định và danh mục người dùng
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = null,
                tint = if (danhMuc.laMacDinh)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(end = 12.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                // Tên danh mục kèm nhãn Mặc định nếu là danh mục hệ thống
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = danhMuc.ten,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    if (danhMuc.laMacDinh) {
                        Text(
                            text = "  (Mặc định)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                // Mô tả danh mục nếu có nội dung
                if (danhMuc.moTa.isNotEmpty()) {
                    Text(
                        text = danhMuc.moTa,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            // Nút xóa chỉ hiển thị với danh mục người dùng tạo (không phải mặc định)
            if (!danhMuc.laMacDinh) {
                IconButton(onClick = onXoa) {
                    Icon(Icons.Default.Delete, contentDescription = "Xóa danh mục")
                }
            }
        }
    }
}