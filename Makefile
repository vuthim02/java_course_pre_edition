.PHONY: build-all test-all clean-all

build-all:
	./gradlew build

test-all:
	./gradlew test

clean-all:
	./gradlew clean
