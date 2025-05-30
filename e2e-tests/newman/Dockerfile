FROM node:16-alpine

# Install Newman and HTML reporter
RUN npm install -g newman@5.3.2 newman-reporter-htmlextra

WORKDIR /etc/newman

# Copy collection and environment files
COPY collections/* ./collections/
COPY environments/* ./environments/

# Create directory for reports
RUN mkdir -p reports

# Entry point
ENTRYPOINT ["newman"]
CMD ["run", "ecommerce-e2e.collection.json", "-r", "cli,htmlextra", "--reporter-htmlextra-export", "reports/report.html", "--disable-unicode"]