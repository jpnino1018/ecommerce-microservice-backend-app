from locust import HttpUser, task, between
import random
import string

def random_string(length=10):
    return ''.join(random.choices(string.ascii_letters + string.digits, k=length))

def random_email():
    return f"{random_string(5)}@example.com"

class EcommerceUser(HttpUser):
    host = "http://4.156.107.102:8080"
    wait_time = between(1, 3)

    @task
    def create_user(self):
        user_id = random.randint(1000, 9999)
        payload = {
            "userId": user_id,
            "firstName": "Alejandro",
            "lastName": "Cordoba",
            "imageUrl": f"https://picsum.photos/200?random={user_id}",
            "email": random_email(),
            "phone": f"+1{random.randint(1000000000,9999999999)}",
            "addressDtos": [
                {
                    "fullAddress": "123 Main St",
                    "postalCode": "12345",
                    "city": "New York"
                }
            ],
            "credential": {
                "username": f"user{user_id}",
                "password": "securePassword123",
                "roleBasedAuthority": "ROLE_USER",
                "isEnabled": True,
                "isAccountNonExpired": True,
                "isAccountNonLocked": True,
                "isCredentialsNonExpired": True
            }
        }

        with self.client.post("/user-service/api/users", json=payload, catch_response=True) as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"Create User failed: {response.status_code}")

    @task
    def get_products(self):
        with self.client.get("/product-service/api/products", catch_response=True) as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"Get Products failed: {response.status_code}")
