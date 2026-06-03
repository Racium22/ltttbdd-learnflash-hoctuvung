package com.example.learnflash.duLieu.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.learnflash.duLieu.local.dao.DanhMucDao
import com.example.learnflash.duLieu.local.dao.LichSuOnTapDao
import com.example.learnflash.duLieu.local.dao.TuVungDao
import com.example.learnflash.duLieu.local.thucThe.DanhMuc
import com.example.learnflash.duLieu.local.thucThe.LichSuOnTap
import com.example.learnflash.duLieu.local.thucThe.TuVung

// Khai báo cơ sở dữ liệu Room với 3 Entity
@Database(
    entities = [TuVung::class, LichSuOnTap::class, DanhMuc::class],
    version = 1,
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

        // Hàm khởi tạo và cung cấp Database
        fun layDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "learnflash_database"
                )
                    // Tự động chèn danh mục mặc định "Chung" khi tạo hoặc mở cơ sở dữ liệu (tự động sửa lỗi cho máy có dữ liệu cũ)
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            db.execSQL(
                                """INSERT OR IGNORE INTO danh_muc (id, ten, moTa, laMacDinh)
                                   VALUES ('mac_dinh', 'Chung', 'Danh mục mặc định cho từ vựng chưa phân loại', 1)"""
                            )
                        }

                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            db.execSQL(
                                """INSERT OR IGNORE INTO danh_muc (id, ten, moTa, laMacDinh)
                                   VALUES ('mac_dinh', 'Chung', 'Danh mục mặc định cho từ vựng chưa phân loại', 1)"""
                            )
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}