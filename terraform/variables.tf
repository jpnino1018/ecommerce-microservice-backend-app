variable "resource_group_name" {
  default = "ecommerce-rg"
}

variable "location" {
  default = "eastus"
}

variable "aks_name" {
  default = "ecommerce-aks"
}

variable "node_count" {
  default = 1
}

variable "vm_size" {
  # default = "Standard_DS2_v2"
  default = "Standard_B2ms"
}
