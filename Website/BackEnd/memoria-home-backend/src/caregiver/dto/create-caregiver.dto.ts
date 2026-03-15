import { CreateUserDto } from "src/Common/create-user.dto";

export class CreateCaregiverDto extends CreateUserDto {

     caregiver_id: string
     first_name: string
     last_name: string
     phone: string
     specialization: string
     licence_number: string
     years_experience: Uint8Array

}
