variable "aks_name" {
  description = "Nombre del AKS cluster"
  type        = string
}

variable "resource_group_name" {
  description = "Nombre del resource group"
  type        = string
}

variable "location" {
  description = "Región de Azure"
  type        = string
}

variable "vnet_subnet_id" {
  description = "ID de la subred de la VNet"
  type        = string
}

variable "node_count" {
  description = "Cantidad de nodos en el pool por defecto"
  type        = number
  default     = 1
}

variable "vm_size" {
  description = "Tamaño de las VMs del pool"
  type        = string
  default     = "Standard_DS2_v2"
}
