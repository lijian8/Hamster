package com.cintcm.hamster.relation.experiment;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.cintcm.hamster.relation.Relation;
import com.cintcm.hamster.relation.SimpleRelationExtractor;
import com.cintcm.hamster.relation.io.excel.RelationRenderer;
import com.cintcm.hamster.relation.io.mysql.MySQLUtils;

public class Jia {
	public static void main(String[] args) {
		try {
			Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
			Properties props = new Properties();
			Properties prop = new Properties();
			// prop.put("charSet", "UTF-8");
			// prop.put("charSet", "UTF-8");

			prop.put("user", "");
			prop.put("password", "");
			prop.put("charSet", "utf-8");
			prop.put("lc_ctype", "utf-8");
			prop.put("encoding", "utf-8");

			// props.put ("charSet", "UTF-8");
			String url = "jdbc:odbc:DRIVER=Microsoft Access Driver (*.mdb, *.accdb);DBQ=e:\\jia.mdb";
			// Connection conn = DriverManager.getConnection(url, "", "");
			Connection conn = DriverManager.getConnection(url, props);

			Statement stmt = conn.createStatement();
			ResultSet resultSet = stmt.executeQuery("select * from wx");

			//int i = 0;
			//List<Relation> relations = new ArrayList<Relation>();
			while (resultSet.next()) {
				String text = new String(resultSet.getBytes(2), "gbk");
				String doc_id = new String(resultSet.getBytes(1), "gbk");

				MySQLUtils.insertRelations("jia", new SimpleRelationExtractor(text, doc_id)
				.getRelations());
				

				//if (i++ > 2)					break;
			}
			//new RelationRenderer(relations, new File("test.xls")).outputFile();
			
			stmt.close();
			conn.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
}