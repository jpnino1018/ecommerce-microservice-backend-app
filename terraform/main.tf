module "network" {
  source              = "./modules/network"
  resource_group_name = var.resource_group_name
  location            = var.location
}

module "aks" {
  source              = "./modules/aks"
  aks_name            = var.aks_name
  resource_group_name = module.network.resource_group_name
  location            = var.location
  vnet_subnet_id      = module.network.subnet_id
}
