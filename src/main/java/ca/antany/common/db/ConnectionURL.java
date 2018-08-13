package ca.antany.common.db;

public enum ConnectionURL {
	LOCAL("jdbc:sqlserver://localhost:1433;databaseName=PROJECT_DB;integratedSecurity=true"),
	LOCAL_TEST("jdbc:sqlserver://localhost:1433;databaseName=PROJECT_DB_TEST;integratedSecurity=true"),
	LOCAL_MASTER("jdbc:sqlserver://localhost:1433;databaseName=master;integratedSecurity=true");
	String connectionURL;
	ConnectionURL(String connectionURL){
		this.connectionURL = connectionURL;
	}
	
	@Override
	public String toString() {
		return this.connectionURL;
	}
}
