FROM gradle:5.5.1-jdk11
COPY . .
CMD exec /bin/bash -c "trap : TERM INT; sleep infinity & wait"