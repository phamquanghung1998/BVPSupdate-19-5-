#pragma once

#ifndef  SQLITE_EXCEPTION
#define SQLITE_EXCEPTION 1
#include "sqlite3.h"

namespace sqlitecrypt {


	void throw_sqlite3_exception(sqlite3* handle);
 
	void throw_sqlite3_exception(const char* message);
 
 
	void throw_sqlite3_exception(sqlite3* handle, const char* message);
 

	/* throw a SQLiteException for a given error code */
	void throw_sqlite3_exception_errcode(int errcode, const char* message);
 

	void throw_sqlite3_exception(int errcode,
		const char* sqlite3Message, const char* message);
	 
}

#endif // ! SQLITE_EXCEPTION
