package service;

import java.sql.*;

public class DatabaseService {

    // ============================================================
    //  CONFIGURATION MYSQL 
    // ============================================================
    private static final String DB_HOST     = "localhost";
    private static final String DB_PORT     = "3306";
    private static final String DB_NAME     = "systeme_rh";
    private static final String DB_USER     = "root";
    private static final String DB_PASSWORD = "";   // votre mot de passe MySQL ici
    // ============================================================

    private static final String DB_URL =
        "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME
        + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8";

    public static void initialize() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                System.out.println("Connexion MySQL etablie sur " + DB_HOST + "/" + DB_NAME);
                createTables(conn);
                insertSampleDataIfEmpty(conn);
            }
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver MySQL non trouve : " + e.getMessage());
        }
    }

    private static void createTables(Connection conn) throws SQLException {
        String[] queries = {
            "CREATE TABLE IF NOT EXISTS employee (" +
            "  id            INT AUTO_INCREMENT PRIMARY KEY," +
            "  nom           VARCHAR(100) NOT NULL," +
            "  prenom        VARCHAR(100) NOT NULL," +
            "  poste         VARCHAR(100)," +
            "  salaire       DOUBLE DEFAULT 0," +
            "  departement   VARCHAR(100)," +
            "  date_embauche TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4",

            "CREATE TABLE IF NOT EXISTS job (" +
            "  id                   INT AUTO_INCREMENT PRIMARY KEY," +
            "  titre                VARCHAR(200) NOT NULL," +
            "  description          TEXT," +
            "  competences_requises TEXT NOT NULL," +
            "  departement          VARCHAR(100)," +
            "  statut               VARCHAR(50) DEFAULT 'Ouverte'," +
            "  date_creation        TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "  salaire_mini         INT DEFAULT 0," +
            "  salaire_maxi         INT DEFAULT 0" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4",

            "CREATE TABLE IF NOT EXISTS candidate (" +
            "  id               INT AUTO_INCREMENT PRIMARY KEY," +
            "  nom              VARCHAR(100) NOT NULL," +
            "  prenom           VARCHAR(100) NOT NULL," +
            "  email            VARCHAR(150)," +
            "  telephone        VARCHAR(30)," +
            "  cv_text          MEDIUMTEXT," +
            "  cv_file_path     VARCHAR(500)," +
            "  date_candidature TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "  adresse          VARCHAR(300)," +
            "  linkedin_url     VARCHAR(300)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4",

            "CREATE TABLE IF NOT EXISTS application (" +
            "  id               INT AUTO_INCREMENT PRIMARY KEY," +
            "  candidate_id     INT NOT NULL," +
            "  job_id           INT NOT NULL," +
            "  score            INT DEFAULT 0," +
            "  recommandation   VARCHAR(50)," +
            "  date_candidature TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "  statut           VARCHAR(50) DEFAULT 'En attente'," +
            "  notes            TEXT," +
            "  FOREIGN KEY (candidate_id) REFERENCES candidate(id) ON DELETE CASCADE," +
            "  FOREIGN KEY (job_id)       REFERENCES job(id)       ON DELETE CASCADE," +
            "  UNIQUE KEY uq_cand_job (candidate_id, job_id)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
        };

        try (Statement stmt = conn.createStatement()) {
            for (String sql : queries) {
                stmt.execute(sql);
            }
            System.out.println("Tables MySQL creees avec succes");
        }
    }

    private static void insertSampleDataIfEmpty(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs   = stmt.executeQuery("SELECT COUNT(*) FROM employee")) {
            if (rs.next() && rs.getInt(1) == 0) {
                insertSampleData(conn);
            }
        }
    }

    private static void insertSampleData(Connection conn) throws SQLException {
        System.out.println("Insertion des donnees de demonstration...");

        String sqlEmp = "INSERT INTO employee (nom, prenom, poste, salaire, departement) VALUES " +
            "('Dupont','Marie','Developpeur Senior',50000,'IT')," +
            "('Martin','Jean','Chef de Projet',60000,'IT')," +
            "('Bernard','Sophie','RH Manager',55000,'RH')," +
            "('Petit','Pierre','Analyste Data',48000,'IT')," +
            "('Durand','Claire','Comptable',45000,'Finance')";

        String sqlJob = "INSERT INTO job (titre, description, competences_requises, departement, salaire_mini, salaire_maxi) VALUES " +
            "('Developpeur Java Senior','Recherche developpeur Java experimente','Java,Spring,SQL,Git','IT',45000,60000)," +
            "('Developpeur Full Stack','Developpeur maitris front et back','JavaScript,React,Node.js,MongoDB','IT',40000,55000)," +
            "('Data Scientist','Analyste de donnees avec ML','Python,Machine Learning,SQL,Statistics','IT',50000,70000)," +
            "('Chef de Projet Digital','Gestion de projets web','Gestion de projet,Agile,Scrum','IT',45000,60000)";

        String[] sqlCands = {
            "INSERT INTO candidate (nom,prenom,email,telephone,cv_text) VALUES ('Rousseau','Thomas','thomas@email.com','0612345678','Thomas Rousseau Developpeur Java Competences Java Spring Boot SQL Git Maven 5 ans experience')",
            "INSERT INTO candidate (nom,prenom,email,telephone,cv_text) VALUES ('Moreau','Julie','julie@email.com','0623456789','Julie Moreau Developpeuse Full Stack Competences JavaScript React Node.js MongoDB HTML CSS 3 ans experience')",
            "INSERT INTO candidate (nom,prenom,email,telephone,cv_text) VALUES ('Lambert','Paul','paul@email.com','0634567890','Paul Lambert Data Scientist Competences Python Machine Learning TensorFlow SQL Statistics 4 ans experience')",
            "INSERT INTO candidate (nom,prenom,email,telephone,cv_text) VALUES ('Girard','Emma','emma@email.com','0645678901','Emma Girard Chef de Projet Certifications PMP Scrum Master Competences Gestion de projet Agile Scrum 6 ans experience')",
            "INSERT INTO candidate (nom,prenom,email,telephone,cv_text) VALUES ('Roux','Lucas','lucas@email.com','0656789012','Lucas Roux Developpeur Junior Competences Java HTML CSS Git 1 an experience')"
        };

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sqlEmp);
            stmt.execute(sqlJob);
            for (String sql : sqlCands) {
                stmt.execute(sql);
            }
            System.out.println("Donnees de demonstration inserees");
        }
    }

    /**
     * Opens and returns a NEW connection each call.The caller MUST close it (use try-with-resources).
     * @return 
     * @throws java.sql.SQLException
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    /** No-op: connections are now per-call; kept for backward compatibility. */
    public static void close() {
        // Nothing to close globally — each caller closes its own connection.
        System.out.println("DatabaseService.close() appelé (connexions par appel, rien à fermer globalement).");
    }

    /**
     * WARNING: The returned ResultSet (and its underlying Connection) must be
     * closed by the caller to avoid resource leaks.Prefer using getConnection()
 with try-with-resources directly.
     * @param query
     * @return 
     * @throws java.sql.SQLException
     */
    public static ResultSet executeQuery(String query) throws SQLException {
        Connection conn = getConnection();
        Statement stmt = conn.createStatement();
        return stmt.executeQuery(query);
    }

    public static int executeUpdate(String query) throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            return stmt.executeUpdate(query);
        }
    }

    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}
