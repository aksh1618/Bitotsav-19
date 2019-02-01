package `in`.bitotsav.shared.exceptions

class NonRetryableException(message: String) : Exception(message)

class DatabaseException(message: String) : Exception(message)

class NetworkException(message: String) : Exception(message)