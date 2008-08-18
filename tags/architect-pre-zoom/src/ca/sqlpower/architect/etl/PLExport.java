package ca.sqlpower.architect.etl;

import java.sql.*;
import java.util.Iterator;
import java.util.Map;
import java.util.LinkedList;
import java.util.HashSet;
import ca.sqlpower.sql.*;
import ca.sqlpower.security.*;
import ca.sqlpower.architect.*;

public class PLExport { 
	public static final String PL_GENERATOR_VERSION = "$Revision$";

	protected DefaultParameters defParam;
	protected String folderName = "Architect Jobs";
	protected String jobId;
	protected String jobDescription;
	protected String jobComment;
	protected DBConnectionSpec plDBCS;
	protected String outputTableOwner;
	protected String plUsername;
	protected String plPassword;
	protected PLSecurityManager sm;

	/**
	 * Creates a folder if one with the name folderName does not exist
	 * already.
	 */
	public void maybeInsertFolder(Connection con) throws SQLException {
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = con.createStatement();
			rs = stmt.executeQuery("SELECT 1 FROM pl_folder WHERE folder_name="
								   +SQL.quote(folderName));
			if (!rs.next()) {
				StringBuffer sql = new StringBuffer("INSERT INTO PL_FOLDER (");
				sql.append("folder_name,folder_desc,folder_status,last_backup_no)");
				sql.append(" VALUES (");
				
				sql.append(SQL.quote(folderName));  // folder_name
				sql.append(",").append(SQL.quote("This Folder id for jobs and transactions created by the Power*Architect"));  // folder_desc
				sql.append(",").append(SQL.quote(null));  // folder_status
				sql.append(",").append(SQL.quote(null));  // last_backup_no
				sql.append(")");
				stmt.executeUpdate(sql.toString());
			}
		} finally {
			if (rs != null) rs.close();
			if (stmt != null) stmt.close();
		}
	}
	
	/**
	 * Inserts an entry in the folderName folder of the named object
	 * of the given type.
	 */
	public void insertFolderDetail(Connection con, String objectType, String objectName)
		throws SQLException {
		StringBuffer sql = new StringBuffer("INSERT INTO PL_FOLDER_DETAIL (");
		sql.append("folder_name,object_type,object_name)");
		sql.append(" VALUES (");

		sql.append(SQL.quote(folderName));  // folder_name
		sql.append(",").append(SQL.quote(objectType));  // object_type
		sql.append(",").append(SQL.quote(objectName));  // object_name
		sql.append(")");
		Statement s = con.createStatement();
		try {
			s.executeUpdate(sql.toString());
		} finally {
			if (s != null) {
				s.close();
			}
		}
	}

	/**
	 * Deletes the PL job with the name set in this.jobId.  Cascades
	 * the delete to all child tables of job, as well as all
	 * transactions in the job.
	 *
	 */
	public void deleteJobCascade(Connection con) throws SQLException, PLSecurityException {
		Statement stmt = con.createStatement();
		ResultSet rs = null;
		try {
			LinkedList jobTransactions = new LinkedList();
			StringBuffer sql = new StringBuffer();
			sql.append("SELECT DISTINCT object_name FROM job_detail");
			sql.append(" WHERE object_type = 'TRANSACTION'");
			sql.append(" AND job_id = ").append(SQL.quote(jobId));
			rs = stmt.executeQuery(sql.toString());
			while (rs.next()) {
				jobTransactions.add(rs.getString(1));
			}
			rs.close();
			
			Iterator it = jobTransactions.iterator();
			while (it.hasNext()) {
				String transId = (String) it.next();
				PLTrans trans = new PLTrans(transId);
				PLSecurityManager.deleteDatabaseObject(con, sm, trans);
				stmt.executeUpdate("DELETE FROM folder_detail WHERE object_type='TRANSACTION'"+
								   " AND object_name="+SQL.quote(transId));
				stmt.executeUpdate("DELETE FROM trans_col_map WHERE trans_id="+SQL.quote(transId));
				stmt.executeUpdate("DELETE FROM trans_table_file_format WHERE trans_id="+SQL.quote(transId));
				stmt.executeUpdate("DELETE FROM trans_table_except_handle WHERE trans_id="+SQL.quote(transId));
				stmt.executeUpdate("DELETE FROM trans_table_pkg WHERE trans_id="+SQL.quote(transId));
				stmt.executeUpdate("DELETE FROM trans_table_file WHERE trans_id="+SQL.quote(transId));
				stmt.executeUpdate("DELETE FROM trans_pkg WHERE trans_id="+SQL.quote(transId));
				stmt.executeUpdate("DELETE FROM trans_except_handle WHERE trans_id="+SQL.quote(transId));
				stmt.executeUpdate("DELETE FROM trans WHERE trans_id="+SQL.quote(transId));
			}

			PLJob job = new PLJob(jobId);
			PLSecurityManager.deleteDatabaseObject(con, sm, job);
			stmt.executeUpdate("DELETE FROM folder_detail WHERE object_type='JOB'"+
							   " AND object_name="+SQL.quote(jobId));
			stmt.executeUpdate("DELETE FROM job_detail WHERE job_id="+SQL.quote(jobId));
			stmt.executeUpdate("DELETE FROM pl_job WHERE job_id="+SQL.quote(jobId));
		} finally {
			if (rs != null) rs.close();
			if (stmt != null) stmt.close();
		}
	}

	/**
	 * Inserts a job into the PL_JOB table.  The job name is specified by {@link #jobId}.
	 *
	 * @param con A connection to the PL database
	 */
	public void insertJob(Connection con) throws SQLException {
		
		StringBuffer sql = new StringBuffer("INSERT INTO PL_JOB (");
		sql.append("JOB_ID, JOB_DESC, JOB_FREQ_DESC, PROCESS_CNT, SHOW_PROGRESS_FREQ, PROCESS_SEQ_CODE, MAX_RETRY_COUNT, WRITE_DB_ERRORS_IND, ROLLBACK_SEGMENT_NAME, LOG_FILE_NAME, ERR_FILE_NAME, UNIX_LOG_FILE_NAME, UNIX_ERR_FILE_NAME, APPEND_TO_LOG_IND, APPEND_TO_ERR_IND, DEBUG_MODE_IND, COMMIT_FREQ, JOB_COMMENT, CREATE_DATE, LAST_update_DATE, LAST_update_USER, BATCH_SCRIPT_FILE_NAME, JOB_SCRIPT_FILE_NAME, UNIX_BATCH_SCRIPT_FILE_NAME, UNIX_JOB_SCRIPT_FILE_NAME, JOB_STATUS, LAST_BACKUP_NO, LAST_RUN_DATE, SKIP_PACKAGES_IND, SEND_EMAIL_IND, LAST_update_OS_USER, STATS_IND, checked_out_ind, checked_out_date, checked_out_user, checked_out_os_user");
		sql.append(") VALUES (");
		sql.append(SQL.quote(jobId));  // JOB_ID
		sql.append(",").append(SQL.quote(jobDescription));  // JOB_DESC
		sql.append(",").append(SQL.quote(null));  // JOB_FREQ_DESC
		sql.append(",").append(SQL.quote(null));  // PROCESS_CNT
		sql.append(",").append(SQL.quote(null));  // SHOW_PROGRESS_FREQ
		sql.append(",").append(SQL.quote(null));  // PROCESS_SEQ_CODE
		sql.append(",").append(SQL.quote(null));  // MAX_RETRY_COUNT
		sql.append(",").append(SQL.quote(null));  // WRITE_DB_ERRORS_IND
		sql.append(",").append(SQL.quote(null));  // ROLLBACK_SEGMENT_NAME
		sql.append(",").append(SQL.quote(fixWindowsPath(defParam.get("default_log_file_path"))+jobId+".log"));  // LOG_FILE_NAME
		sql.append(",").append(SQL.quote(fixWindowsPath(defParam.get("default_err_file_path"))+jobId+".err"));  // ERR_FILE_NAME
		sql.append(",").append(SQL.quote(fixUnixPath(defParam.get("default_log_file_path"))+jobId+".log"));  // UNIX_LOG_FILE_NAME
		sql.append(",").append(SQL.quote(fixUnixPath(defParam.get("default_err_file_path"))+jobId+".err"));  // UNIX_ERR_FILE_NAME
		sql.append(",").append(SQL.quote("N"));  // APPEND_TO_LOG_IND
		sql.append(",").append(SQL.quote("N"));  // APPEND_TO_ERR_IND
		sql.append(",").append(SQL.quote("N"));  // DEBUG_MODE_IND
		sql.append(",").append("100"); 			 // COMMIT_FREQ
		sql.append(",").append(SQL.quote(jobComment));  // JOB_COMMENT
		sql.append(",").append(SQL.escapeDate(con, new java.util.Date()));  // CREATE_DATE
		sql.append(",").append(SQL.escapeDate(con, new java.util.Date()));  // LAST_update_DATE
		sql.append(",").append(SQL.quote(con.getMetaData().getUserName()));  // LAST_update_USER
		sql.append(",").append(SQL.quote(null));  // BATCH_SCRIPT_FILE_NAME
		sql.append(",").append(SQL.quote(null));  // JOB_SCRIPT_FILE_NAME
		sql.append(",").append(SQL.quote(null));  // UNIX_BATCH_SCRIPT_FILE_NAME
		sql.append(",").append(SQL.quote(null));  // UNIX_JOB_SCRIPT_FILE_NAME
		sql.append(",").append(SQL.quote(null));  // JOB_STATUS
		sql.append(",").append(SQL.quote(null));  // LAST_BACKUP_NO
		sql.append(",").append(SQL.quote(null));  // LAST_RUN_DATE
		sql.append(",").append(SQL.quote(null));  // SKIP_PACKAGES_IND
		sql.append(",").append(SQL.quote("N"));  // SEND_EMAIL_IND
		sql.append(",").append(SQL.quote(System.getProperty("user.name")));  // LAST_update_OS_USER
		sql.append(",").append(SQL.quote("N"));  // STATS_IND
		sql.append(",").append(SQL.quote(null));  // checked_out_ind
		sql.append(",").append(SQL.quote(null));  // checked_out_date
		sql.append(",").append(SQL.quote(null));  // checked_out_user
		sql.append(",").append(SQL.quote(null));  // checked_out_os_user
		sql.append(")");
		Statement s = con.createStatement();
		try {
			s.executeUpdate(sql.toString());
		} finally {
			if (s != null) {
				s.close();
			}
		}
	}		

	/**
	 * Inserts a job entry into the JOB_DETAIL table.  The job name is
	 * specified by {@link #jobId}.
	 *
	 * @param con A connection to the PL database
	 */
	public void insertJobDetail(Connection con, int seqNo, String objectType, String objectName) throws SQLException {

		StringBuffer sql= new StringBuffer("INSERT INTO JOB_DETAIL (");
		sql.append("JOB_ID, JOB_PROCESS_SEQ_NO, OBJECT_TYPE, OBJECT_NAME, JOB_DETAIL_COMMENT, LAST_update_DATE, LAST_update_USER, FAILURE_ABORT_IND, WARNING_ABORT_IND, PKG_PARAM, ACTIVE_IND, LAST_update_OS_USER )");
		sql.append(" VALUES (");
		sql.append(SQL.quote(jobId));  // JOB_ID
		sql.append(",").append(seqNo);  // JOB_PROCESS_SEQ_NO
		sql.append(",").append(SQL.quote(objectType));  // OBJECT_TYPE
		sql.append(",").append(SQL.quote(objectName));  // OBJECT_NAME
		sql.append(",").append(SQL.quote("Generated by POWER*Architect "+PL_GENERATOR_VERSION));  // JOB_DETAIL_COMMENT
		sql.append(",").append(SQL.escapeDate(con, new java.util.Date()));  // LAST_update_DATE
		sql.append(",").append(SQL.quote(con.getMetaData().getUserName()));  // LAST_update_USER
		sql.append(",").append(SQL.quote("N"));  // FAILURE_ABORT_IND
		sql.append(",").append(SQL.quote("N"));  // WARNING_ABORT_IND
		sql.append(",").append(SQL.quote(null));  // PKG_PARAM
		sql.append(",").append(SQL.quote("Y"));  // ACTIVE_IND
		sql.append(",").append(SQL.quote(System.getProperty("user.name")));  // LAST_update_OS_USER
		sql.append(")");
		Statement s = con.createStatement();
		try {
			s.executeUpdate(sql.toString());
		} finally {
			if (s != null) {
				s.close();
			}
		}
	}

	/**
	 * Inserts a Power*Loader transaction header into the TRANS
	 * table.
	 *
	 * @param con A connection to the PL database
	 * @param transId the name that the new transaction should have.
	 * @param t The SQLTable that describes the DB table this
	 * transaction will populate.
	 */
	public void insertTrans(Connection con, String transId, String remarks) throws ArchitectException, SQLException {
		StringBuffer sql = new StringBuffer();
		sql.append(" INSERT INTO TRANS (");
		sql.append("TRANS_ID, TRANS_DESC, TRANS_COMMENT, ACTION_TYPE, MAX_RETRY_COUNT, PROCESS_SEQ_CODE, LAST_update_DATE, LAST_update_USER, DEBUG_MODE_IND, COMMIT_FREQ, PROCESS_ADD_IND, PROCESS_UPD_IND, PROCESS_DEL_IND, WRITE_DB_ERRORS_IND, ROLLBACK_SEGMENT_NAME, ERR_FILE_NAME, LOG_FILE_NAME, BAD_FILE_NAME, SHOW_PROGRESS_FREQ, SKIP_CNT, PROCESS_CNT, SOURCE_DATE_FORMAT, CREATE_DATE, UNIX_LOG_FILE_NAME, UNIX_ERR_FILE_NAME, UNIX_BAD_FILE_NAME, REMOTE_CONNECTION_STRING, APPEND_TO_LOG_IND, APPEND_TO_ERR_IND, APPEND_TO_BAD_IND, REC_DELIMITER, TRANS_STATUS, LAST_BACKUP_NO, LAST_RUN_DATE, SKIP_PACKAGES_IND, SEND_EMAIL_IND, PROMPT_COLMAP_INDEXES_IND, TRANSACTION_TYPE, DELTA_SORT_IND, LAST_update_OS_USER, STATS_IND, ODBC_IND, checked_out_ind, checked_out_date, checked_out_user, checked_out_os_user )");

		sql.append( "VALUES (");
		sql.append(SQL.quote(transId));
		sql.append(",").append(SQL.quote("Generated by Power*Architect "+PL_GENERATOR_VERSION));
		sql.append(",").append(SQL.quote(remarks));
		sql.append(",").append(SQL.quote(null));
		sql.append(",").append(SQL.quote(null));
		sql.append(",").append(SQL.quote(null));
		sql.append(",").append(SQL.escapeDate(con, new java.util.Date()));
		sql.append(",").append(SQL.quote(con.getMetaData().getUserName()));
		sql.append(",").append(SQL.quote("N"));
		sql.append(",").append(defParam.get("commit_freq"));
		sql.append(",").append(SQL.quote(null));
		sql.append(",").append(SQL.quote(null));
		sql.append(",").append(SQL.quote(null));
		sql.append(",").append(SQL.quote(null));
		sql.append(",").append(SQL.quote(null));
		sql.append(",").append(SQL.quote(fixWindowsPath(defParam.get("default_err_file_path"))+transId+".err"));
		sql.append(",").append(SQL.quote(fixWindowsPath(defParam.get("default_log_file_path"))+transId+".log"));
		sql.append(",").append(SQL.quote(fixWindowsPath(defParam.get("default_bad_file_path"))+transId+".bad"));
		sql.append(",").append(defParam.get("show_progress_freq"));
		sql.append(",").append("0");
		sql.append(",").append("0");
		sql.append(",").append(SQL.quote(null));
		sql.append(",").append(SQL.escapeDate(con, new java.util.Date()));
		sql.append(",").append(SQL.quote(fixUnixPath(defParam.get("default_unix_log_file_path"))+transId+".log"));
		sql.append(",").append(SQL.quote(fixUnixPath(defParam.get("default_unix_err_file_path"))+transId+".err"));
		sql.append(",").append(SQL.quote(fixUnixPath(defParam.get("default_unix_bad_file_path"))+transId+".bad"));
		sql.append(",").append(SQL.quote(null)); //REMOTE_CONNECTION_STRING
		sql.append(",").append(SQL.quote(defParam.get("append_to_log_ind"))); // APPEND_TO_LOG_IND
		sql.append(",").append(SQL.quote(defParam.get("append_to_err_ind"))); // APPEND_TO_ERR_IND
		sql.append(",").append(SQL.quote(defParam.get("append_to_bad_ind"))); // APPEND_TO_BAD_IND
		sql.append(",").append(SQL.quote(null)); // REC_DELIMITER
		sql.append(",").append(SQL.quote(null)); // TRANS_STATUS
		sql.append(",").append(SQL.quote(null)); // LAST_BACKUP_NO
		sql.append(",").append(SQL.quote(null)); // LAST_RUN_DATE
		sql.append(",").append(SQL.quote("N")); // SKIP_PACKAGES_IND
		sql.append(",").append(SQL.quote("N")); // SEND_EMAIL_IND
		sql.append(",").append(SQL.quote(null)); // PROMPT_COLMAP_INDEXES_IND
		sql.append(",").append(SQL.quote("POWER_LOADER")); // TRANSACTION_TYPE
		sql.append(",").append(SQL.quote("Y")); // DELTA_SORT_IND
		sql.append(",").append(SQL.quote(System.getProperty("user.name"))); // LAST_update_OS_USER
		sql.append(",").append(SQL.quote("N")); // STATS_IND
		sql.append(",").append(SQL.quote("Y")); // ODBC_IND
		sql.append(",").append(SQL.quote(null)); // checked_out_ind
		sql.append(",").append(SQL.quote(null)); // checked_out_date
		sql.append(",").append(SQL.quote(null)); // checked_out_user
		sql.append(",").append(SQL.quote(null)); // checked_out_os_user
		sql.append(")");
		Statement s = con.createStatement();
		try {
			s.executeUpdate(sql.toString());
		} finally {
			if (s != null) {
				s.close();
			}
		}
	}

	/**
	 * Inserts a record into the TRANS_TABLE_FILE table.
	 *
	 * @param con The connection to the PL databse.
	 * @param transId The name of the header transaction.
	 * @param table The SQLTable that this record describes.
	 * @param isOuput True if table is an output table (part of the
	 * play pen); false if table is an input table (part of a source
	 * DB).
	 * @param seqNo The sequence number of this table in its parent
	 * transaction.
	 */
	public void insertTransTableFile(Connection con,
									 String transId,
									 String tableFileId,
									 SQLTable table,
									 boolean isOutput,
									 int seqNo) throws SQLException {
		StringBuffer sql= new StringBuffer("INSERT INTO TRANS_TABLE_FILE (");
		sql.append("TRANS_ID, TABLE_FILE_ID, TABLE_FILE_IND, TABLE_FILE_TYPE, INPUT_OUTPUT_IND, SYSTEM_NAME, SERVER_NAME, FILE_CHAR_SET, TEXT_DELIMITER, TEXT_QUALIFIER, OWNER, TABLE_FILE_NAME, TABLE_FILE_ACCESS_PATH, MAX_ADD_COUNT, MAX_UPD_COUNT, MAX_DEL_COUNT, MAX_ERR_COUNT, FILTER_CRITERION, PROC_SEQ_NO, HEADER_REC_IND, LAST_UPDATE_DATE, LAST_UPDATE_USER, TRANS_TABLE_FILE_COMMENT, DB_CONNECT_NAME, UNIX_FILE_ACCESS_PATH, REC_DELIMITER, SELECT_CLAUSE, FROM_CLAUSE, WHERE_CLAUSE, ORDER_BY_CRITERION, TRUNCATE_IND, ACTION_TYPE, ANALYZE_IND, PRE_PROCESSED_FILE_NAME, UNIX_PRE_PROCESSED_FILE_NAME, PARENT_FILE_ID, CHILD_REQUIRED_IND, LAST_UPDATE_OS_USER, DELETE_IND, FROM_CLAUSE_DB)");

		sql.append(" VALUES (");
		sql.append(SQL.quote(transId));  // TRANS_ID
		sql.append(",").append(SQL.quote(tableFileId));  // TABLE_FILE_ID
		sql.append(",").append(SQL.quote("TABLE"));  // TABLE_FILE_IND

		String type;
		DBConnectionSpec dbcs;
		if (isOutput) {
			dbcs = plDBCS;
		} else {
			dbcs = table.getParentDatabase().getConnectionSpec();
		}

		if (isOracle(dbcs)) {
			type = "ORACLE";
		} else if (isSQLServer(dbcs)) {
			type = "SQL SERVER";
		} else if (isDB2(dbcs)) {
			type = "DB2";
		} else {
			throw new IllegalArgumentException("Unsupported target database type");
		}
		sql.append(",").append(SQL.quote(type));  // TABLE_FILE_TYPE

		sql.append(",").append(SQL.quote(isOutput ? "O" : "I"));  // INPUT_OUTPUT_IND
		sql.append(",").append(SQL.quote(null));  // SYSTEM_NAME
		sql.append(",").append(SQL.quote(null));  // SERVER_NAME
		sql.append(",").append(SQL.quote(null));  // FILE_CHAR_SET
		sql.append(",").append(SQL.quote(null));  // TEXT_DELIMITER
		sql.append(",").append(SQL.quote(null));  // TEXT_QUALIFIER
		sql.append(",").append(SQL.quote(outputTableOwner));  // OWNER
		sql.append(",").append(SQL.quote(table.getName()));  // TABLE_FILE_NAME
		sql.append(",").append(SQL.quote(null));  // TABLE_FILE_ACCESS_PATH
		sql.append(",").append(SQL.quote(null));  // MAX_ADD_COUNT
		sql.append(",").append(SQL.quote(null));  // MAX_UPD_COUNT
		sql.append(",").append(SQL.quote(null));  // MAX_DEL_COUNT
		sql.append(",").append(SQL.quote(null));  // MAX_ERR_COUNT
		sql.append(",").append(SQL.quote(null));  // FILTER_CRITERION
		sql.append(",").append(seqNo);  // PROC_SEQ_NO
		sql.append(",").append(SQL.quote(null));  // HEADER_REC_IND
		sql.append(",").append(SQL.escapeDate(con, new java.util.Date()));  // LAST_update_DATE
		sql.append(",").append(SQL.quote(con.getMetaData().getUserName()));  // LAST_update_USER
		sql.append(",").append(SQL.quote("Generated by Power*Architect "+PL_GENERATOR_VERSION));  // TRANS_TABLE_FILE_COMMENT
		sql.append(",").append(SQL.quote(null));  // DB_CONNECT_NAME FIXME: important!
		sql.append(",").append(SQL.quote(null));  // UNIX_FILE_ACCESS_PATH
		sql.append(",").append(SQL.quote(null));  // REC_DELIMITER
		sql.append(",").append(SQL.quote(null));  // SELECT_CLAUSE
		sql.append(",").append(SQL.quote(null));  // FROM_CLAUSE
		sql.append(",").append(SQL.quote(null));  // WHERE_CLAUSE
		sql.append(",").append(SQL.quote(null));  // ORDER_BY_CRITERION
		sql.append(",").append(SQL.quote(null));  // TRUNCATE_IND
		sql.append(",").append(SQL.quote(null));  // ACTION_TYPE
		sql.append(",").append(SQL.quote(null));  // ANALYZE_IND
		sql.append(",").append(SQL.quote(null));  // PRE_PROCESSED_FILE_NAME
		sql.append(",").append(SQL.quote(null));  // UNIX_PRE_PROCESSED_FILE_NAME
		sql.append(",").append(SQL.quote(null));  // PARENT_FILE_ID
		sql.append(",").append(SQL.quote(null));  // CHILD_REQUIRED_IND
		sql.append(",").append(System.getProperty("user.name"));  // LAST_UPDATE_OS_USER
		sql.append(",").append(SQL.quote(null));  // DELETE_IND
		sql.append(",").append(SQL.quote(null));  // FROM_CLAUSE_DB
		sql.append(")");
		Statement s = con.createStatement();
		try {
			s.executeUpdate(sql.toString());
		} finally {
			if (s != null) {
				s.close();
			}
		}
	}
	
	/**
	 * Inserts mapping records (by calling insertTransColMap) for all
	 * mandatory columns of outputTable, as well as all columns of
	 * outputTable whose source table is inputTable.
	 */
	public void insertMappings(Connection con,
							   String transId,
							   String outputTableId,
							   SQLTable outputTable,
							   String inputTableId,
							   SQLTable inputTable) throws SQLException, ArchitectException {
		int seqNo = 1;
		Iterator outCols = outputTable.getColumns().iterator();
		while (outCols.hasNext()) {
			SQLColumn outCol = (SQLColumn) outCols.next();
			SQLColumn sourceCol = outCol.getSourceColumn();
			if ( (outCol.getNullable() == DatabaseMetaData.columnNoNulls)  // also covers PK
				 || (sourceCol != null && sourceCol.getParentTable() == inputTable) ) {
				insertTransColMap(con, transId, outputTableId, outCol, inputTableId, seqNo);
				seqNo++;
			}
		}
	}

	/**
	 * Inserts a column mapping record for outputColumn into the
	 * TRANS_COL_MAP table.
	 *
	 * @param con The connection to the PL database.
	 * @param transId The transaction name.
	 * @param transTableFileId The transaction file id of the output table.
	 * @param outputColumn The column to generate a mapping for.
	 * @param inputTableMap A map with SQLTable objects as keys and
	 * Strings with the corresponding TRANS_TABLE_FILE_ID for that
	 * input table.
	 * @param seqNo The sequence number of the output table in trans_table_file
	 */
	public void insertTransColMap(Connection con,
								  String transId,
								  String outputTableId,
								  SQLColumn outputColumn,
								  String inputTableId,
								  int seqNo) throws SQLException {
		SQLColumn inputColumn = outputColumn.getSourceColumn();
		String inputColumnName;
		if (inputColumn != null) {
			inputColumnName = inputColumn.getName();
		} else {
			inputColumnName = null;
		}
		StringBuffer sql= new StringBuffer("INSERT INTO TRANS_COL_MAP (");
		sql.append("TRANS_ID, INPUT_TABLE_FILE_ID, INPUT_TRANS_COL_NAME, OUTPUT_TABLE_FILE_ID, OUTPUT_TRANS_COL_NAME, VALID_ACTION_TYPE, NATURAL_ID_IND, REAL_MEM_TRANS_IND, DEFAULT_VALUE, INPUT_TRANS_VALUE, OUTPUT_TRANS_VALUE, TRANS_TABLE_NAME, SEQ_NAME, GRP_FUNC_STRING, TRANS_COL_MAP_COMMENT, PROCESS_SEQ_NO, LAST_update_DATE, LAST_update_USER, OUTPUT_PROC_SEQ_NO, TRANSLATION_VALUE, ACTIVE_IND, PL_SEQ_IND, PL_SEQ_INCREMENT, LAST_update_OS_USER, TRANSFORMATION_CRITERIA, PL_SEQ_update_TABLE_IND, SEQ_TABLE_IND, SEQ_WHERE_CLAUSE)");
		sql.append(" VALUES (");

		sql.append(SQL.quote(transId));  // TRANS_ID
		sql.append(",").append(SQL.quote(inputTableId)); //INPUT_TABLE_FILE_ID
		sql.append(",").append(SQL.quote(inputColumnName));  // INPUT_TRANS_COL_NAME
		sql.append(",").append(SQL.quote(outputTableId));  // OUTPUT_TABLE_FILE_ID
		sql.append(",").append(SQL.quote(outputColumn.getName()));  // OUTPUT_TRANS_COL_NAME
		sql.append(",").append(SQL.quote(outputColumn.getPrimaryKeySeq() != null ? "A" : "AU"));  // VALID_ACTION_TYPE
		sql.append(",").append(SQL.quote(outputColumn.getPrimaryKeySeq() != null ? "Y" : "N"));  // NATURAL_ID_IND
		sql.append(",").append(SQL.quote(null));  // REAL_MEM_TRANS_IND
		sql.append(",").append(SQL.quote(outputColumn.getDefaultValue()));  // DEFAULT_VALUE
		sql.append(",").append(SQL.quote(null));  // INPUT_TRANS_VALUE
		sql.append(",").append(SQL.quote(null));  // OUTPUT_TRANS_VALUE
		sql.append(",").append(SQL.quote(null));  // TRANS_TABLE_NAME
		sql.append(",").append(SQL.quote(null));  // SEQ_NAME
		sql.append(",").append(SQL.quote(null));  // GRP_FUNC_STRING
		sql.append(",").append(SQL.quote("Generated by Power*Architect "+PL_GENERATOR_VERSION));  // TRANS_COL_MAP_COMMENT
		sql.append(",").append(SQL.quote(null));  // PROCESS_SEQ_NO
		sql.append(",").append(SQL.escapeDate(con, new java.util.Date()));  // LAST_update_DATE
		sql.append(",").append(SQL.quote(con.getMetaData().getUserName()));  // LAST_update_USER
		sql.append(",").append(seqNo);  // OUTPUT_PROC_SEQ_NO  from trans_table_file.seq_no?
		sql.append(",").append(SQL.quote(null));  // TRANSLATION_VALUE
		sql.append(",").append(SQL.quote("Y"));  // ACTIVE_IND
		sql.append(",").append(SQL.quote(null));  // PL_SEQ_IND
		sql.append(",").append(SQL.quote(null));  // PL_SEQ_INCREMENT
		sql.append(",").append(SQL.quote(System.getProperty("user.name")));  // LAST_update_OS_USER
		sql.append(",").append(SQL.quote(null));  // TRANSFORMATION_CRITERIA
		sql.append(",").append(SQL.quote(null));  // PL_SEQ_update_TABLE_IND
		sql.append(",").append(SQL.quote(null));  // SEQ_TABLE_IND
		sql.append(",").append(SQL.quote(null));  // SEQ_WHERE_CLAUSE
		Statement s = con.createStatement();
		try {
			s.executeUpdate(sql.toString());
		} finally {
			if (s != null) {
				s.close();
			}
		}
	}

	/**
	 * Inserts a transaction exception handler into the
	 * TRANS_EXCEPT_HANDLE table.  You specify the action type as one
	 * of ACTION_TYPE_ADD, ACTION_TYPE_UPDATE, or ACTION_TYPE_DELETE
	 * and this method figures out the rest for you.
	 *
	 * @param con A connection to the PL database
	 * @param actionType the action type to insert.
	 * @param transId the transaction to add this exception handler to.
	 */
	public void insertTransExceptHandler(Connection con, String actionType,  String transId) throws SQLException {

		int errorCode = 0;
		String resultActionType;

		if (DBConnection.isOracle(con)) {
			if(actionType.equals("A")) {
				errorCode = -1;
				resultActionType="CHANGE_TO_UPD";
			} else if(actionType.equals("U")) {
				errorCode = 1403;
				resultActionType="CHANGE_TO_ADD";
			} else if(actionType.equals("D")) {
				errorCode = 1403;
				resultActionType="SKIP";
			} else {
				throw new IllegalArgumentException("Invalid Action type " + actionType); 
			}
		} else if (DBConnection.isSQLServer(con)) {
			if(actionType.equals("A")) {
				errorCode = -2627;
				resultActionType="CHANGE_TO_UPD";
			} else if(actionType.equals("U")) {
				errorCode = 100;
				resultActionType="CHANGE_TO_ADD";
			} else if(actionType.equals("D")) {
				errorCode = 100;
				resultActionType="SKIP";
			} else {
				throw new IllegalArgumentException("Invalid Action type " + actionType); 
			}
		} else if (DBConnection.isDB2(con)) {
			if(actionType.equals("A")) {
				errorCode = -803;
				resultActionType="CHANGE_TO_UPD";
			} else if(actionType.equals("U")) {
				errorCode = 100;
				resultActionType="CHANGE_TO_ADD";
			} else if(actionType.equals("D")) {
				errorCode = 100;
				resultActionType="SKIP";
			} else {
				throw new IllegalArgumentException("Invalid Action type " + actionType); 
			}
		} else {
			throw new IllegalArgumentException("Unsupported Target Database type");
		}
		StringBuffer sql= new StringBuffer("INSERT INTO TRANS_EXCEPT_HANDLE (");
		sql.append("TRANS_ID,INPUT_ACTION_TYPE,DBMS_ERROR_CODE,RESULT_ACTION_TYPE,EXCEPT_HANDLE_COMMENT,LAST_update_DATE,LAST_update_USER,PKG_NAME,PKG_PARAM,PROC_FUNC_IND,ACTIVE_IND,LAST_update_OS_USER)");
	    sql.append(" VALUES (");
		sql.append(SQL.quote(transId));	// TRANS_ID
		sql.append(",").append(SQL.quote(actionType));	// INPUT_ACTION_TYPE
		sql.append(",").append(errorCode);	// DBMS_ERROR_CODE
		sql.append(",").append(SQL.quote(resultActionType));	// RESULT_ACTION_TYPE
		sql.append(",").append(SQL.quote("Generated by Power*Architect "+PL_GENERATOR_VERSION));	//EXCEPT_HANDLE_COMMENT
		sql.append(",").append(SQL.escapeDate(con, new java.util.Date()));  // LAST_update_DATE
		sql.append(",").append(SQL.quote(con.getMetaData().getUserName()));  // LAST_update_USER
		sql.append(",").append(SQL.quote(null));	// PKG_NAME
		sql.append(",").append(SQL.quote(null));	// PKG_PARAM
		sql.append(",").append(SQL.quote(null));    // PROC_FUNC_IND
		sql.append(",").append(SQL.quote("Y"));     // ACTIVE_IND
		sql.append(",").append(SQL.quote(System.getProperty("user.name"))); // LAST_update_OS_USER
		sql.append(")");
		Statement s = con.createStatement();
		try {
			s.executeUpdate(sql.toString());
		} finally {
			if (s != null) {
				s.close();
			}
		}
	}

	/**
	 * Does the actual insertion of the PL metadata records into the PL database.
	 */
	public void export(SQLDatabase db) throws SQLException, ArchitectException {
		SQLDatabase target = new SQLDatabase(plDBCS); // we are exporting db into this
		Connection con = null;
		try {
			target.connect();
			con = target.getConnection();
			try {
				defParam=new DefaultParameters(con);
			} catch (PLSchemaException p) {
				throw new ArchitectException("couldn't load default parameters",p);
			}

			sm = new PLSecurityManager(con, plUsername, plPassword);

			maybeInsertFolder(con);
			deleteJobCascade(con);
			insertJob(con);
			Iterator tables = db.getChildren().iterator();
			int outputTableNum = 1;
			while(tables.hasNext()) {
				SQLTable outputTable = (SQLTable) tables.next();
				HashSet inputTables = new HashSet();
				
				Iterator cols = outputTable.getColumns().iterator();
				while (cols.hasNext()) {
					SQLColumn outputCol = (SQLColumn) cols.next();
					SQLColumn inputCol = outputCol.getSourceColumn();
					int transNum = 1;

					// looking for unique input tables of outputTable
					if (inputCol != null && inputTables.contains(inputCol.getParentTable())) {
						SQLTable inputTable = inputCol.getParentTable();
						inputTables.add(inputTable);
						String transName = "LOAD_"+outputTable.getName()+"_"+transNum;
						insertTrans(con, transName, outputTable.getRemarks());
						insertTransExceptHandler(con, "A", transName);
						insertTransExceptHandler(con, "U", transName);
						insertTransExceptHandler(con, "D", transName);
						insertJobDetail(con, outputTableNum*10, "TRANSACTION", transName);

						String outputTableId = outputTable.getName()+"_OUT_"+outputTableNum;
						String inputTableId = inputTable.getName()+"_IN_"+transNum;
						insertTransTableFile(con, transName, outputTableId, outputTable, true, transNum);
						insertTransTableFile(con, transName, inputTableId, inputTable, false, transNum);
						insertMappings(con, transName, outputTableId, outputTable, inputTableId, inputTable);
						transNum++;
					}
				}
				outputTableNum++;
			}
		} finally {
			if (con != null) con.close();
		}

	}


	// --------------------------- UTILITY METHODS ------------------------
	protected String fixWindowsPath(String path) {
		if (path == null) {
			path="";
		} else if ( ! path.endsWith("\\")){
			path +="\\";
		}
		return path;
	}

	protected String fixUnixPath(String path) {
		if (path == null) {
			path="";
		} else if ( ! path.endsWith("/")){
			path +="/";
		}
		return path;
	}

	protected boolean isOracle(DBConnectionSpec dbcs) {
		if(dbcs.getDriverClass().toLowerCase().indexOf("oracledriver") >= 0) {
			return true;
		} else {
			return false;
		}
	}

	protected boolean isSQLServer(DBConnectionSpec dbcs) {
		if(dbcs.getDriverClass().toLowerCase().indexOf("sqlserver") >= 0) {
			return true;
		} else {
			return false;
		}
	}

	protected boolean isDB2(DBConnectionSpec dbcs) {
		if(dbcs.getDriverClass().toLowerCase().indexOf("db2") >= 0) {
			return true;
		} else {
			return false;
		}
	}

	public static class PLJob implements DatabaseObject {

		public String jobId;

		public PLJob(String jobId) {
			this.jobId = jobId;
		}

		public String getObjectType() {
			return "JOB";
		}

		public String getObjectName() {
			return jobId;
		}
	}

	public static class PLTrans implements DatabaseObject {

		public String transId;

		public PLTrans(String transId) {
			this.transId = transId;
		}

		public String getObjectType() {
			return "TRANSACTION";
		}

		public String getObjectName() {
			return transId;
		}
	}
}