FROM postman/newman:latest

# Install HTML reporter
RUN npm install -g newman-reporter-htmlextra

WORKDIR /etc/newman

# Copy collection file
COPY ecommerce-e2e.collection.json collection.json

# Create directory for reports
RUN mkdir -p reports

ENTRYPOINT ["newman"]
CMD ["run", "collection.json", "-r", "cli,htmlextra", "--reporter-htmlextra-export", "reports/report.html", "--disable-unicode"]