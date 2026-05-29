package com.example.learnflash.duLieu.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.learnflash.duLieu.local.dao.DanhMucDao
import com.example.learnflash.duLieu.local.dao.LichSuOnTapDao
import com.example.learnflash.duLieu.local.dao.TuVungDao
import com.example.learnflash.duLieu.local.thucThe.DanhMuc
import com.example.learnflash.duLieu.local.thucThe.LichSuOnTap
import com.example.learnflash.duLieu.local.thucThe.TuVung

// Khai báo cơ sở dữ liệu Room với 3 Entity và tăng version lên 2 do thay đổi schema
@Database(
    entities = [TuVung::class, LichSuOnTap::class, DanhMuc::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // Cung cấp phương thức để lấy đối tượng truy vấn TuVungDao
    abstract fun tuVungDao(): TuVungDao

    // Cung cấp phương thức để lấy đối tượng truy vấn LichSuOnTapDao
    abstract fun lichSuOnTapDao(): LichSuOnTapDao

    // Cung cấp phương thức để lấy đối tượng truy vấn DanhMucDao
    abstract fun danhMucDao(): DanhMucDao

    companion object {
        // Đảm bảo chỉ có một Instance duy nhất của Database (Singleton Pattern)
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Migration từ version 1 lên version 2 — thêm bảng danh_muc và cột danhMucId vào tu_vung
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Tạo bảng danh_muc mới với cấu trúc tương ứng Entity DanhMuc
                database.execSQL(
                    """CREATE TABLE IF NOT EXISTS danh_muc (
                        id TEXT NOT NULL PRIMARY KEY,
                        ten TEXT NOT NULL,
                        moTa TEXT NOT NULL DEFAULT '',
                        laMacDinh INTEGER NOT NULL DEFAULT 0
                    )"""
                )
                // Thêm cột danhMucId vào bảng tu_vung hiện có với giá trị mặc định
                database.execSQL(
                    "ALTER TABLE tu_vung ADD COLUMN danhMucId TEXT NOT NULL DEFAULT 'mac_dinh'"
                )
                // Chèn danh mục mặc định để các từ cũ có danh mục hợp lệ
                database.execSQL(
                    """INSERT OR IGNORE INTO danh_muc (id, ten, moTa, laMacDinh)
                       VALUES ('mac_dinh', 'Chung', 'Danh mục mặc định cho từ vựng chưa phân loại', 1)"""
                )
            }
        }

        // Hàm khởi tạo và cung cấp Database kèm Migration
        fun layDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "learnflash_database"
                )
                    // Đăng ký Migration để Room tự động nâng cấp schema khi phát hiện version mới
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}