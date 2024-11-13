import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;  
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;

public class DatabaseHelper {

    private Connection conn;

    // Koneksi ke SQLite dan buat file database jika belum ada
    public void connect() {
        try {
            // Buat koneksi ke database (file akan dibuat jika belum ada)
            String url = "jdbc:sqlite:kontak.db";
            conn = DriverManager.getConnection(url);

            if (conn != null) {
                System.out.println("Koneksi ke SQLite berhasil.");
                createTable(); // Buat tabel jika belum ada
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // Membuat tabel kontak jika belum ada
    public void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS kontak ("
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " nama TEXT NOT NULL,"
                + " nomor_telepon TEXT NOT NULL,"
                + " kategori TEXT NOT NULL"
                + ");";

        try {
            Statement stmt = conn.createStatement();
            stmt.execute(sql);
            System.out.println("Tabel kontak berhasil dibuat atau sudah ada.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // Tutup koneksi
    public void closeConnection() {
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void main(String[] args) {
        DatabaseHelper dbHelper = new DatabaseHelper();
        dbHelper.connect(); // Membuat koneksi dan file database
        dbHelper.closeConnection(); // Menutup koneksi
    }

    public void insertKontak(String nama, String telpon, String kategori) {
        if (!isValidPhoneNumber(telpon)) {
            System.out.println("Nomor telepon tidak valid. Harus hanya berisi angka dan panjangnya antara 10 hingga 13 karakter.");
            return; 
        }

        String sql = "INSERT INTO kontak(nama, nomor_telepon, kategori) VALUES(?, ?, ?)";
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, nama);
            pstmt.setString(2, telpon);
            pstmt.setString(3, kategori);
            pstmt.executeUpdate();
            System.out.println("Kontak berhasil ditambahkan.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public ResultSet getKontak() {
        String sql = "SELECT * FROM kontak";
        ResultSet rs = null;
        try {
            Statement stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return rs;
    }

    public void updateKontak(int id, String nama, String telpon, String kategori) {
        if (!isValidPhoneNumber(telpon)) {
            System.out.println("Nomor telepon tidak valid. Harus hanya berisi angka dan panjangnya antara 10 hingga 13 karakter.");
            return;
        }
        String sql = "UPDATE kontak SET nama = ?, nomor_telepon = ?, kategori = ? WHERE id = ?";
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, nama);
            pstmt.setString(2, telpon);
            pstmt.setString(3, kategori);
            pstmt.setInt(4, id);
            pstmt.executeUpdate();
            System.out.println("Kontak berhasil diperbarui.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void deleteKontak(int id) {
        String sql = "DELETE FROM kontak WHERE id = ?";
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            System.out.println("Kontak berhasil dihapus.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public boolean isValidPhoneNumber(String phoneNumber) {
        // Memastikan hanya angka dan panjang antara 10-13
        return phoneNumber.matches("\\d{10,13}");
    }

    public void exportToCSV(String filePath) {
        String sql = "SELECT * FROM kontak";
        try (FileWriter csvWriter = new FileWriter(filePath)) {
            // Menulis header CSV
            csvWriter.append("ID,Nama,Nomor Telepon,Kategori\n");

            // Menulis data kontak ke file CSV
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                int id = rs.getInt("id");
                String nama = rs.getString("nama");
                String nomorTelepon = rs.getString("nomor_telepon");
                String kategori = rs.getString("kategori");

                // Menulis setiap baris data kontak ke CSV
                csvWriter.append(id + "," + nama + "," + nomorTelepon + "," + kategori + "\n");
            }

            System.out.println("Kontak berhasil diekspor ke " + filePath);
        } catch (SQLException | IOException e) {
            System.out.println("Error saat mengekspor ke CSV: " + e.getMessage());
        }
    }

    public void importFromCSV(String filePath) {
        String sql = "INSERT INTO kontak(nama, nomor_telepon, kategori) VALUES(?, ?, ?)";

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            // Lewati header CSV
            br.readLine();

            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 4) {
                    String nama = data[1];
                    String nomorTelepon = data[2];
                    String kategori = data[3];

                    // Masukkan data kontak ke dalam database
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1, nama);
                    pstmt.setString(2, nomorTelepon);
                    pstmt.setString(3, kategori);
                    pstmt.executeUpdate();
                }
            }

            System.out.println("Kontak berhasil diimpor dari " + filePath);
        } catch (IOException | SQLException e) {
            System.out.println("Error saat mengimpor dari CSV: " + e.getMessage());
        }
    }

    ResultSet searchKontak(String nama, String kategori) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
