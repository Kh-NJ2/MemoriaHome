import { Injectable } from '@nestjs/common';
import { CreateCaregiverDto } from '../caregiver/dto/create-caregiver.dto'

@Injectable()
export class AuthService {

    signup(createCaregiverDto: CreateCaregiverDto) {
        //sql to add the caregiver
        //exception handling
  return createCaregiverDto;
}

}

