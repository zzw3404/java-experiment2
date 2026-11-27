package study.enterprise.mis;

final class Failure extends RuntimeException {
    final int status;

    Failure(int status, String message) {
        super(message);
        this.status = status;
    }
}
