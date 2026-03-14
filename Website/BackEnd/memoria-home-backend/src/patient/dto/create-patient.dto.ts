export class CreatePatientDto {
    patient_id: string
    first_name: string
    last_name: string
    date_of_birth: Date
    gender: 'Male' | 'Female'
}
