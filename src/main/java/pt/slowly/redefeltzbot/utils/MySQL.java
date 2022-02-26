package pt.slowly.redefeltzbot.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import pt.slowly.redefeltzbot.Main;

public class MySQL {

	public static Connection con = null;
	
	public static void abrirConexao() {
		String str1 = Main.database.getString("MySQL.IP");
		int i = Main.database.getInt("MySQL.Porta");
		String str2 = Main.database.getString("MySQL.Usuario");
		String str3 = Main.database.getString("MySQL.Senha");
		String str4 = Main.database.getString("MySQL.DataBase");
		String str5 = "jdbc:mysql://";
		String str6 = String.valueOf(String.valueOf(str5)) + str1 + ":" + i + "/" + str4;
		try {
			
			Class.forName("com.mysql.jdbc.Driver");
			con = DriverManager.getConnection(str6, str2, str3);
			criarTabela();
			System.out.println("Conexão com o MySQL foi bem sucedida!");

		} catch (ClassNotFoundException | SQLException e) {
			
                    e.printStackTrace();
                    System.out.println("Ocorreu um erro ao tentar conectar ao MySQL!");
			
		}
	}
	
	public static void criarTabela() {
		PreparedStatement preparedStatement = null;
		
		try {
			
			preparedStatement = con.prepareStatement("CREATE TABLE IF NOT EXISTS TAGS(JOGADOR VARCHAR(45), TAG TEXT);");
			preparedStatement.executeUpdate();
			System.out.println("Tabela carregada com sucesso!");
			
		} catch (SQLException e) {
			System.out.println("Ocorreu um erro ao tentar carregar a tabela!");
		}
        }
	
	public static void addJogador(String jogador, String tag) {
		PreparedStatement preparedStatement = null;
		
		try {
		
			preparedStatement = con.prepareStatement("INSERT INTO TAGS (JOGADOR,TAG) VALUES (?,?)");
			preparedStatement.setString(1, jogador);
                        preparedStatement.setString(2, tag);
			preparedStatement.executeUpdate();
			
		} catch (SQLException e) {
			// TODO: handle exception
		}
	}
	
	public static boolean hasJogador(String jogador) {
		PreparedStatement preparedStatement = null;
		
		try {
			
			preparedStatement = con.prepareStatement("SELECT * FROM TAGS WHERE JOGADOR = ?");
			preparedStatement.setString(1, jogador);
			ResultSet rs = preparedStatement.executeQuery();
			boolean result = rs.next();
			return result;
			
		} catch (SQLException e) {
			return false;
		}
	}
	
	public static void removeJogador(String jogador) {
		PreparedStatement preparedStatement = null;
		
		if (hasJogador(jogador)) {
			try {
				
				preparedStatement = con.prepareStatement("DELETE FROM TAGS WHERE JOGADOR = ?");
				preparedStatement.setString(1, jogador);
				preparedStatement.executeUpdate();
				
			} catch (SQLException e) {
				// TODO: handle exception
			}
		}
	}
	
	public static void setTag(String jogador, String tag) {
		
		PreparedStatement preparedStatement = null;
		if (hasJogador(jogador)) {
			try {
				
				preparedStatement = con.prepareStatement("UPDATE TAGS SET TAG = ? WHERE JOGADOR = ?");
				preparedStatement.setString(2, jogador);
				preparedStatement.setString(1, tag);
				preparedStatement.executeUpdate();
				
			} catch (SQLException e) {
				// TODO: handle exception
			}
		}else {
			addJogador(jogador, tag);
		}
		
	}
	
	public static String getTag(String jogador) {
		
		PreparedStatement preparedStatement = null;
		try {
			
			preparedStatement = con.prepareStatement("SELECT TAG FROM TAGS WHERE JOGADOR = ?");
			preparedStatement.setString(1, jogador);
			ResultSet rs = preparedStatement.executeQuery();
			if (rs.next())
				return rs.getString("TAG"); 
			return null;
		} catch (SQLException e) {
			return null;
		}
	}
	
}
