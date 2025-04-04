FROM ubuntu:22.04

# Set environment variables to prevent interactive prompts
ENV DEBIAN_FRONTEND=noninteractive
ENV TZ=Etc/UTC

# Install prerequisites
RUN apt-get update && \
    apt-get install -y git openjdk-17-jdk maven postgresql postgresql-contrib sudo

# Clone repository
RUN git clone https://github.com/benvessely/recipe.git /app

# Copy data files into the container filesystem
COPY ./data /app/src/main/resources/nutrition_data/

# Set working directory
WORKDIR /app

# Configure database
COPY setup-and-test.sh /app/
RUN chmod +x /app/setup-and-test.sh

# Use this as entry point
ENTRYPOINT ["/app/setup-and-test.sh"]
