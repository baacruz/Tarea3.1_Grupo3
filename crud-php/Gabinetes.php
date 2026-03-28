<?php
class Gabinetes
{

    private $conexion;
    private $table = "gabinetes";

    public $id;
    public $nombre;
    public $direccion;
    public $latitud;
    public $longitud;
    public $submask;
    public $gateway;

    // Constructor de la clase personas
    public function __construct($db)
    {
        $this->conexion = $db;
    }


    // Create
    public function createGabinete()
    {
        $consulta = "INSERT INTO 
                    " . $this->table . "
                    SET 
                    nombre = :nombre,
                    direccion = :direccion,
                    latitud = :latitud,
                    longitud = :longitud,
                    submask = :submask,
                    gateway = :gateway";

        $comando = $this->conexion->prepare($consulta);

        // Sanitizacion
        $this->nombre = htmlspecialchars(strip_tags($this->nombre));
        $this->direccion = htmlspecialchars(strip_tags($this->direccion));
        $this->latitud = htmlspecialchars(strip_tags($this->latitud));
        $this->longitud = htmlspecialchars(strip_tags($this->longitud));
        $this->submask = htmlspecialchars(strip_tags($this->submask));
        $this->gateway = htmlspecialchars(strip_tags($this->gateway));

        // bind data
        $comando->bindParam(":nombre", $this->nombre);
        $comando->bindParam(":direccion", $this->direccion);
        $comando->bindParam(":latitud", $this->latitud);
        $comando->bindParam(":longitud", $this->longitud);
        $comando->bindParam(":submask", $this->submask);
        $comando->bindParam(":gateway", $this->gateway);

        if($comando->execute())
        {
            return true;
        }
        return false;
    }

    // Read
     
    public function GetListGabinetes()
    {
        $consulta = "SELECT * FROM " . $this->table . "";
        $comando = $this->conexion->prepare($consulta);
        $comando->execute();

        return $comando;
    }

    // Update
    
    public function updateGabinete()
    {
        $consulta = "UPDATE " . $this->table . " SET 
                        nombre = :nombre,
                        direccion = :direccion,
                        latitud = :latitud,
                        longitud = :longitud,
                        submask = :submask,
                        gateway = :gateway
                    WHERE id = :id";

        $comando = $this->conexion->prepare($consulta);

        // Sanitización
        $this->nombre = htmlspecialchars(strip_tags($this->nombre));
        $this->direccion = htmlspecialchars(strip_tags($this->direccion));
        $this->latitud = htmlspecialchars(strip_tags($this->latitud));
        $this->longitud = htmlspecialchars(strip_tags($this->longitud));
        $this->submask = htmlspecialchars(strip_tags($this->submask));
        $this->gateway = htmlspecialchars(strip_tags($this->gateway));
        $this->id = htmlspecialchars(strip_tags($this->id));

        
        // Binding de datos
        $comando->bindParam(":nombre", $this->nombre);
        $comando->bindParam(":direccion", $this->direccion);
        $comando->bindParam(":latitud", $this->latitud);
        $comando->bindParam(":longitud", $this->longitud);
        $comando->bindParam(":submask", $this->submask);
        $comando->bindParam(":gateway", $this->gateway);
        $comando->bindParam(':id', $this->id);

       if($comando->execute())
        {
            return true;
        }
        return false;
    }


    // Delete

    public function deleteGabinete()
    {
        $consulta = "DELETE FROM " . $this->table . " WHERE id = :id";

        $comando = $this->conexion->prepare($consulta);

        // Sanitización
        $this->id = htmlspecialchars(strip_tags($this->id));

        // Binding
        $comando->bindParam(':id', $this->id);

         if($comando->execute())
        {
            return true;
        }
        return false;
    }

}



?>